package com.jefferson.regah;

import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Peer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

class TorrentSeeder {
    private static final Logger log = LogManager.getLogger(TorrentSeeder.class);
    private final ImmutableWrapper<Client> clientWrapper;

    TorrentSeeder() {
        this.clientWrapper = new ImmutableWrapper<>();
    }

    Peer seedSharedTorrent(int seedTime, SharedTorrent torrent, InetAddress localAddress) throws IOException {
        final Client client = clientWrapper.set(new Client(localAddress, torrent));
        log.info("Seeder# listening on " + client.getPeerSpec());

        client.addObserver((observable, data) -> {
            Client client1 = (Client) observable;
            Client.ClientState state = (Client.ClientState) data;
            float progress = client1.getTorrent().getCompletion();
            log.debug("Seeder# State:" + state + " Progress update: " + progress);
        });

        log.info("seeder# starts seeding");
        client.share(seedTime);

        return client.getPeerSpec();
    }

    void stop() {
        log.debug("Seeder# is stopping");
        clientWrapper.get().stop(false);
    }
}