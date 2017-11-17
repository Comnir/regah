package com.jefferson.regah;

import com.jefferson.regah.dto.PeerDto;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class InvestigateTorrentingWithoutTracker {
    private static final Logger log = LogManager.getLogger(InvestigateTorrentingWithoutTracker.class);

    private final InetAddress localAddress;
    private final AtomicBoolean downloaderIsDone;
    private final BlockingQueue<Peer> seedingPeerQ;

    private InvestigateTorrentingWithoutTracker(String ip) throws UnknownHostException {
        this.localAddress = InetAddress.getByName(ip);
        downloaderIsDone = new AtomicBoolean(false);
        seedingPeerQ = new LinkedBlockingQueue<>(1);
    }

    private void createTorrentFile(final File sharedPath, final File outputTorrent)
            throws InterruptedException, NoSuchAlgorithmException, IOException {
        final Torrent newTorrent = SharedTorrent.create(sharedPath, null, "regah");
        try (final OutputStream os = new FileOutputStream(outputTorrent)) {
            newTorrent.save(os);
        }
    }

    private void seedTorrent(final File torrentFile, final File parentOfShared,
                             final int seedTime) throws IOException, NoSuchAlgorithmException {
        final SharedTorrent torrent = SharedTorrent.fromFile(torrentFile, parentOfShared);
        Client client = new Client(localAddress, torrent);
        log.info("Seeder# listening on " + client.getPeerSpec());
        seedingPeerQ.add(client.getPeerSpec());

        client.addObserver((observable, data) -> {
            Client client1 = (Client) observable;
            Client.ClientState state = (Client.ClientState) data;
            float progress = client1.getTorrent().getCompletion();
            log.debug("Seeder# State:" + state + " Progress update: " + progress);
        });

        log.info("seeder# starts seeding");
        client.share(seedTime);

        waitForDownloader();
        log.debug("Seeder# is stopping");
        client.stop(false);
        log.debug("Seeder# is Done");
    }

    private void downloadTorrent(final File torrentFile, final File destination)
            throws IOException, NoSuchAlgorithmException {
        log.debug("Download to " + destination + "; exists: " + destination.exists());
        final SharedTorrent torrent = SharedTorrent.fromFile(torrentFile, destination);
        Client client = new Client(localAddress, torrent);
        log.info("Downloader# got seeder listening on " + client.getPeerSpec());

        client.addObserver((observable, data) -> {
            Client client1 = (Client) observable;
            Client.ClientState state = (Client.ClientState) data;
            float progress = client1.getTorrent().getCompletion();
            log.debug("Downloader# State:" + state + " Progress update: " + progress);
        });

        log.info("downloader# starts download");
        client.download();

        while (!client.readyForConnection()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("donwloader# Interrupted!");
                Thread.interrupted();
                return;
            }
        }
        log.info("downloader# connection handler is ready!");

        final Peer seederLocalPeer;
        try {
            seederLocalPeer = seedingPeerQ.take();
        } catch (InterruptedException e) {
            log.error("downloader# was interrupted while waiting for peer!");
            Thread.currentThread().interrupt();
            return;
        }

        log.info("downloader# got peer " + seederLocalPeer);

        final String jsonRemotePeer = PeerDto.peerToJson(seederLocalPeer);
        log.info("Peer as json:" + jsonRemotePeer);
        final Peer seederPeerForRemote = PeerDto.jsonToPeer(jsonRemotePeer);

        client.handleDiscoveredPeers(List.of(seederPeerForRemote));
        client.waitForCompletion();
        downloaderIsDone.set(true);
        log.debug("Downloader# is stopping");
        client.stop(false);
        log.debug("Downloader# is Done");
    }

    private void waitForDownloader() {
        while (!downloaderIsDone.get()) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for downloader!");
                return;
            }
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        final InvestigateTorrentingWithoutTracker investigator = new InvestigateTorrentingWithoutTracker("127.0.0.1");

        final File sharedFile = File.createTempFile("sharedFile", "txt", null);
        final Path source = Paths.get(investigator.getClass()
                .getResource("/share/ForSharing.txt")
                .getPath());
        Files.copy(source, sharedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        sharedFile.deleteOnExit();

        final File torrentFile = File.createTempFile("singleFile", "torrent");
        torrentFile.deleteOnExit();

        investigator.createTorrentFile(sharedFile, torrentFile);

        final CompletingExecutor executor = new CompletingExecutor();

        executor.submit(() -> {
            try {
                investigator.seedTorrent(torrentFile, sharedFile.getParentFile(), 300);
            } catch (Exception e) {
                throw new RuntimeException("Error in seeder", e);
            }
        });

        final File tempDestination = new File(torrentFile.getParentFile(), "downloadedTorrent");
        tempDestination.mkdir();
        tempDestination.deleteOnExit();
        executor.submit(() -> {
            try {
                investigator.downloadTorrent(torrentFile, tempDestination);
            } catch (Exception e) {
                throw new RuntimeException("Error in downloader", e);
            }
        });

        executor.shutdownWhenComplete();

        log.info("Main# is done");
    }
}
