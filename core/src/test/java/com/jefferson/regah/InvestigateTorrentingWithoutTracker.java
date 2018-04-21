package com.jefferson.regah;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class InvestigateTorrentingWithoutTracker {
    private static final Logger log = LogManager.getLogger(InvestigateTorrentingWithoutTracker.class);

    private final InetAddress localAddress;
    private final AtomicBoolean downloaderIsDone;
    private final BlockingQueue<Peer> seedingPeerQ;
    private final TorrentSeeder torrentSeeder;
    private final TorrentDownloader torrentDownloader = new TorrentDownloader();

    private InvestigateTorrentingWithoutTracker(String ip) throws UnknownHostException {
        this.localAddress = InetAddress.getByName(ip);
        downloaderIsDone = new AtomicBoolean(false);
        seedingPeerQ = new LinkedBlockingQueue<>(1);
        torrentSeeder = new TorrentSeeder();
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

        seedingPeerQ.add(torrentSeeder.seedSharedTorrent(seedTime, torrent, localAddress));
        waitForDownloader();
        torrentSeeder.stop();
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

    private void downloadTorrent(final File torrentFile, final File destination)
            throws IOException, NoSuchAlgorithmException {
        log.debug("Download to " + destination + "; exists: " + destination.exists());
        final SharedTorrent torrent = SharedTorrent.fromFile(torrentFile, destination);
        final Peer seederLocalPeer;
        try {
            seederLocalPeer = seedingPeerQ.take();
        } catch (InterruptedException e) {
            log.error("downloader# was interrupted while waiting for peer!");
            Thread.currentThread().interrupt();
            return;
        }

        log.info("downloader# got peer " + seederLocalPeer);
        torrentDownloader.downloadSharedTorrent(torrent, seederLocalPeer, localAddress);
        downloaderIsDone.set(true);
        log.debug("Downloader# is Done");
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
                System.out.println("Done downloading!");
            } catch (Exception e) {
                throw new RuntimeException("Error in downloader", e);
            }
        });

        executor.shutdownWhenComplete();

        MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] originalHash = md.digest(Files.readAllBytes(sharedFile.toPath()));
        final File targetFile = new File(tempDestination, sharedFile.getName());
        final byte[] resultHash = md.digest(Files.readAllBytes(targetFile.toPath()));
        assertArrayEquals(originalHash, resultHash);
        log.info("Main# is done");
    }
}
