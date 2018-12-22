package com.jefferson.regah.transport.torrent;

import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.transport.*;
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
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TorrentTransporter implements Transporter {
    private static final Logger log = LogManager.getLogger(TorrentTransporter.class);
    private static final String ANY_ADDRESS = "0.0.0.0";

    private final Map<String, Client> seeders;
    private final InetAddress localAddress;

    public TorrentTransporter() {
        seeders = new ConcurrentHashMap<>(5);
        try {
            this.localAddress = InetAddress.getByName(ANY_ADDRESS);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Failed to initialize InetAddress for local host!");
        }
    }

    @Override
    public TransportData dataForDownloading(File file) throws FailureToPrepareForDownload {
        final String uuid = UUID.randomUUID().toString();
        try {
            final File torrentFile = File.createTempFile("download-" + uuid + "-", ".torrent");
            final Torrent torrent;
            final SharedTorrent sharedTorrent;
            if (file.isFile()) {
                torrent = Torrent.create(file, null, "regah");
                sharedTorrent = new SharedTorrent(torrent, file.getParentFile(), true);
            } else {
                final List<File> fileListing = listFolderContentsRecursively(file);
                torrent = Torrent.create(file, fileListing, null, "regah");
                sharedTorrent = new SharedTorrent(torrent, file.getParentFile(), true);
            }
            try (final OutputStream os = new FileOutputStream(torrentFile)) {
                torrent.save(os);
            }

            final Client client = new Client(localAddress, sharedTorrent);
            final Peer localAsPeer = client.getPeerSpec();

            client.addObserver((observable, data) -> {
                float progress = ((Client) observable).getTorrent().getCompletion();
                log.debug("Seeder# State: " + data + " Progress update: " + progress);
            });

            client.share();
            seeders.put(uuid, client);
            return new TorrentTransportData(uuid, localAsPeer, torrent.getEncoded());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final String message = "Interrupted while preparing file for download";
            log.warn(message, e);
            throw new FailureToPrepareForDownload(message, e);
        } catch (IOException | NoSuchAlgorithmException e) {
            final String message = "Error encountered while preparing file for download";
            log.warn(message, e);
            throw new FailureToPrepareForDownload(message, e);
        }
    }

    private List<File> listFolderContentsRecursively(final File folder) throws IOException {
        try (final Stream<Path> folderContents = Files.walk(folder.toPath())) {
            return folderContents
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void downloadWithData(final TransportData downloadData, final Path destination) {
        final ImmutableWrapper<TorrentTransportData> dataWrapper = new ImmutableWrapper<>();
        downloadData.acceptVisitor(new TransporterProvidingVisitor(dataWrapper));
        if (dataWrapper.isEmpty()) {
            throw new IllegalArgumentException("Unsupported Transport data provided to Torrent transporter.");
        }
        final TorrentTransportData data = dataWrapper.get();

        log.info("Downloader got torrent data - download ID: " + data.getId());

        final SharedTorrent torrent;
        try {
            //            torrent = new SharedTorrent(new Torrent(data.getTorrentData(), false), destination.toFile(), false);
            torrent = new SharedTorrent(/*torrentImmutableWrapper.get()*/data.getTorrentData(), destination.toFile(), false);
        } catch (IOException | NoSuchAlgorithmException e) {
            final String message = String.format("Failed to parse transport torrent data %s", e.getMessage());
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        log.info("Downloader created a torrent file. Will download to " + destination);
        final Peer remotePeer;
        try {
            remotePeer = data.getSeedingPeer();
            log.info(String.format("Downloader got remote peer info %s", remotePeer));
        } catch (InvalidTransportData invalidTransportData) {
            throw new IllegalArgumentException("Got torrent trasport data, but it was malformed.", invalidTransportData);
        }
        final InetAddress address;
        try {
            address = InetAddress.getByName(ANY_ADDRESS);
        } catch (UnknownHostException e) {
            final String message = "Trying to get localhost address failed.";
            throw new RuntimeException(message, e);
        }
        download(torrent, remotePeer, address);

        log.info("Completed downloading torrent " + data.getId());
    }

    private void download(SharedTorrent torrent, Peer remotePeer, InetAddress address) {
        final Client client;
        try {
            client = new Client(address, torrent);
        } catch (IOException e) {
            final String message = String.format("Failed to create a torrent client to download data. %s", e.getMessage());

            throw new RuntimeException(message, e);
        }

        client.addObserver((observable, rawState) -> {
            Client client1 = (Client) observable;
            Client.ClientState state = (Client.ClientState) rawState;
            float progress = client1.getTorrent().getCompletion();
            log.debug("Downloader# State:" + state + " Progress update: " + progress);
        });

        log.info("downloader# starts download");
        client.download();

        log.info("Downloader created a torrent client. Will wait for the client to be ready for connections.");
        final long timeout = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        while (!client.readyForConnection() && timeout > System.currentTimeMillis()) {
            try {
                Thread.sleep(1000);
                log.trace("Downloader - client not ready for connection.");
            } catch (InterruptedException e) {
                log.error("downloader# Interrupted!");
                Thread.currentThread().interrupt();
            }
        }
        if (!client.readyForConnection()) {
            client.stop();
            throw new RuntimeException("Timed out waiting for torrent client to be ready for connection!");
        }
        log.info(String.format("Downloader will connect to peer %s.", remotePeer));
        client.handleDiscoveredPeers(List.of(remotePeer));
        log.debug("Will wait for download to complete");
        client.waitForCompletion();
    }

    static class TransporterProvidingVisitor implements TransportDataVisitor {
        private final ImmutableWrapper<TorrentTransportData> dataWrapper;

        TransporterProvidingVisitor(ImmutableWrapper<TorrentTransportData> dataWrapper) {
            this.dataWrapper = dataWrapper;
        }

        @Override
        public void visit(TorrentTransportData data) {
            dataWrapper.set(data);
        }

        @Override
        public void visit(TransportData data) {
            // data format not supported by Torrent Transporter
        }


    }
}
