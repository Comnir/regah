package com.jefferson.regah;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class InvestigateTorrenting {
    private static final Logger log = LogManager.getLogger(InvestigateTorrenting.class);

    private final InetAddress localAddress;
    private final URI uri;
    private final int trackerPort;
    private final AtomicBoolean downloaderIsDone;

    private InvestigateTorrenting(String ip, int trackerPort) throws URISyntaxException, UnknownHostException {
        this.trackerPort = trackerPort;
        this.localAddress = InetAddress.getByName(ip);
        final String announceUrl = "http://" + ip + ":" + trackerPort + "/announce";
        uri = new URI(announceUrl);
        downloaderIsDone = new AtomicBoolean(false);
    }

    private void createTorrentFile(final File sharedPath, final File outputTorrent)
            throws InterruptedException, NoSuchAlgorithmException, IOException {
        final Torrent newTorrent = SharedTorrent.create(sharedPath, uri, "regah");
        try (final OutputStream os = new FileOutputStream(outputTorrent)) {
            newTorrent.save(os);
        }
    }

    private void seedTorrent(final File torrentFile, final File parentOfShared,
                             final int seedTime) throws IOException, NoSuchAlgorithmException {
        final SharedTorrent torrent = SharedTorrent.fromFile(torrentFile, parentOfShared);
        Client client = new Client(localAddress, torrent);
        log.info("Seeder# listening on " + client.getPeerSpec());
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
        client.waitForCompletion();
        downloaderIsDone.set(true);
        log.debug("Downloader# is stopping");
        client.stop(false);
        log.debug("Downloader# is Done");
    }

    private void runTrackerSharingTorrent(final File torrentFile) throws IOException, NoSuchAlgorithmException {
        final Tracker tracker = new Tracker(new InetSocketAddress(localAddress, trackerPort));

        log.info("tracker# will announce");
        tracker.announce(TrackedTorrent.load(torrentFile));

        log.info("tracker# start");
        tracker.start();
        waitForDownloader();

        log.info("tracker# stopping");
        tracker.stop();

        log.info("tracker# is done");
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
        final InvestigateTorrenting investigator = new InvestigateTorrenting("127.0.0.1", 9000);


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
                investigator.runTrackerSharingTorrent(torrentFile);
            } catch (Exception e) {
                log.error("Error in tracker", e);
                throw new RuntimeException("Error in tracker", e);
            }
        });

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
                log.error("Error in downloader", e);
                throw new RuntimeException("Error in downloader", e);
            }
        });

        executor.shutdownWhenComplete();
        log.info("Main# is done");
    }
}
