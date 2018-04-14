package com.jefferson.regah.transport;

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
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentTransporter implements Transporter {
    private static final Logger log = LogManager.getLogger(TorrentTransporter.class);

    private final Map<String, Client> seeders;
    private final InetAddress localAddress;
    private File parentFolderForTorrent;

    public TorrentTransporter(File parentFolderForTorrent) throws UnknownHostException {
        this.parentFolderForTorrent = parentFolderForTorrent;
        seeders = new ConcurrentHashMap<>();
        this.localAddress = InetAddress.getByName("localhost");
    }

    @Override
    public TransportData getDownloadInfoFor(File file) throws FailureToPrepareForDownload {
        final String uuid = UUID.randomUUID().toString();
        try {
            final File torrentFile = File.createTempFile("download-" + uuid + "-", ".torrent");
            final Torrent torrent = Torrent.create(file, null, "regah");
            try (final OutputStream os = new FileOutputStream(torrentFile)) {
                torrent.save(os);
            }

            final SharedTorrent sharedTorrent = new SharedTorrent(torrent, parentFolderForTorrent, true);
            final Client client = new Client(localAddress, sharedTorrent);
            final Peer localAsPeer = client.getPeerSpec();

            client.addObserver((observable, data) -> {
                Client client1 = (Client) observable;
                Client.ClientState clientState = (Client.ClientState) data;
                float progress = client1.getTorrent().getCompletion();
                log.debug("Seeder# State: " + clientState + " Progress update: " + progress);
            });

            client.share();
            seeders.put(uuid, client);

            return new TorrentTransportData(uuid, localAsPeer);
        } catch (InterruptedException e) {
            Thread.interrupted();
            final String message = "Interrupted while preparing file for download";
            log.warn(message, e);
            throw new FailureToPrepareForDownload(message, e);
        } catch (IOException | NoSuchAlgorithmException e) {
            final String message = "Error encountered while preparing file for download";
            log.warn(message, e);
            throw new FailureToPrepareForDownload(message, e);
        }
    }
}
