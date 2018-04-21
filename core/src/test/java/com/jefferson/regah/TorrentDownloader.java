package com.jefferson.regah;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Peer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

class TorrentDownloader {
    private static final Logger log = LogManager.getLogger(TorrentDownloader.class);

    TorrentDownloader() {
    }

    void downloadSharedTorrent(final SharedTorrent torrent, final Peer remotePeer, InetAddress localAddress) throws IOException {
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
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.info("downloader# connection handler is ready!");

//        final String jsonRemotePeer = PeerDto.peerToJson(remotePeer);
//        log.info("Peer as json:" + jsonRemotePeer);
//        final Peer seederPeerForRemote = PeerDto.jsonToPeer(jsonRemotePeer);

        log.info("downloader# peer from JSON " + remotePeer);
        client.handleDiscoveredPeers(List.of(remotePeer));
        client.waitForCompletion();

        log.debug("Downloader# is stopping after completion");
        client.stop(false);
    }
}