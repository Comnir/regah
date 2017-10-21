package com.jefferson.regah;

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
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class InvestigateTorrentingWithoutTracker {
    private static final Logger log = LogManager.getLogger(InvestigateTorrentingWithoutTracker.class);

    private final InetAddress localAddress;
    private final AtomicBoolean downloaderIsDone;
    private final AtomicReference<Peer> seedingPeer;

    private InvestigateTorrentingWithoutTracker(String ip) throws URISyntaxException, UnknownHostException {
        this.localAddress = InetAddress.getByName(ip);
        downloaderIsDone = new AtomicBoolean(false);
        seedingPeer = new AtomicReference<>();
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
        synchronized (seedingPeer) {
            seedingPeer.set(client.getPeerSpec());
            seedingPeer.notifyAll();
        }
        client.addObserver((observable, data) -> {
            Client client1 = (Client) observable;
            float progress = client1.getTorrent().getCompletion();
            log.debug("Seeder# Progress update: " + progress);
        });

        log.info("seeder# starts seeding");
        client.share(seedTime);

        waitForDownloader();
        log.debug("Seeder# is stopping");
        client.stop(false);
        log.debug("Seeder# is Done");
    }

    private void downloadTorrent(final File torrentFile, final File destination) throws IOException, NoSuchAlgorithmException {
        log.debug("Download to " + destination + "; exists: " + destination.exists());
        final SharedTorrent torrent = SharedTorrent.fromFile(torrentFile, destination);
        Client client = new Client(localAddress, torrent);
        log.info("Seeder# listening on " + client.getPeerSpec());

        client.addObserver((observable, data) -> {
            Client client1 = (Client) observable;
            float progress = client1.getTorrent().getCompletion();
            log.debug("Downloader# Progress update: " + progress);
        });

        log.info("donwloader# starts download");
        client.download();

        synchronized (seedingPeer) {
            while (null == seedingPeer.get()) {
                try {
                    seedingPeer.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.info("downloader# got peer " + seedingPeer.get());
        final Peer seederLocalPeer =  seedingPeer.get();
        final Peer seederPeerForRemote = new Peer(new InetSocketAddress(seederLocalPeer.getAddress(), seederLocalPeer.getPort()));
        while (!client.readyForConnection()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Interrupted!");
                Thread.interrupted();
            }
        }
        log.info("downloader# connection handler is ready!");

        client.handleDiscoveredPeers(Arrays.asList(seederPeerForRemote));
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
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException, NoSuchAlgorithmException, InterruptedException {
        final InvestigateTorrentingWithoutTracker investigator = new InvestigateTorrentingWithoutTracker("127.0.0.1");

        ExecutorService executorService = Executors.newCachedThreadPool();

        final File sharedFile = File.createTempFile("sharedFile", "txt", null);
        final Path source = Paths.get(investigator.getClass()
                .getResource("/sharedFoldersBasicSharingTest/ForSharing.txt")
                .getPath());
        Files.copy(source, sharedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        sharedFile.deleteOnExit();

        final File torrentFile = File.createTempFile("singleFile", "torrent");
        torrentFile.deleteOnExit();

        investigator.createTorrentFile(sharedFile, torrentFile);

        executorService.submit(() -> {
            try {
                investigator.seedTorrent(torrentFile, sharedFile.getParentFile(), 300);
            } catch (IOException e) {
                log.error("IO Exception in tracker", e);
            } catch (NoSuchAlgorithmException e) {
                log.error("Internal error in seeder", e);
            } catch (Exception e) {
                log.error("Error in seeder", e);
                throw e;
            }
        });

        final File tempDestination = new File(torrentFile.getParentFile(), "downloadedTorrent");
        tempDestination.mkdir();
        tempDestination.deleteOnExit();
        executorService.submit(() -> {
            try {
                investigator.downloadTorrent(torrentFile, tempDestination);
            } catch (IOException e) {
                log.error("IO Exception in tracker", e);
            } catch (NoSuchAlgorithmException e) {
                log.error("Internal error in seeder", e);
            } catch (Exception e) {
                log.error("Error in downloader", e);
                throw e;
            }
        });

        investigator.waitForDownloader();
        log.info("Main# downloader finished, shutting down executor");
        executorService.shutdown();
        final int waitSeconds = 15;
        if (!executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS)) {
            log.info("Main# executor didn't shutdown after " + waitSeconds + " seconds");
            executorService.shutdownNow();
            if (!executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS)) {
                log.warn("Some tasks didn't finish while trying to shutdown!");
            }
        }
        log.info("Main# is done");
    }
}
