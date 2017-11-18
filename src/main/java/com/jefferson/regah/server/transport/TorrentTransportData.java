package com.jefferson.regah.server.transport;

import com.google.gson.Gson;
import com.jefferson.regah.dto.PeerDto;
import com.turn.ttorrent.common.Peer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.Optional;

public class TorrentTransportData implements TransportData {
    private static final Logger log = LogManager.getLogger(TorrentTransportData.class);
    private static final Gson gson = new Gson();

    private final String id;
    private final PeerDto seedingPeereerDto;

    TorrentTransportData(final String id, final Peer seedingPeer) {
        this.id = id;
        this.seedingPeereerDto = PeerDto.fromPeer(seedingPeer);
    }

    public Peer getSeedingPeer() throws InvalidTransportData {
        try {
            return seedingPeereerDto.toPeer();
        } catch (UnknownHostException e) {
            final String error = "Failed to create peer for remote machine, because host is unknown.";
            log.error(error, e);
            throw new InvalidTransportData(error, e);
        }
    }

    @Override
    public String asJson() {
        return gson.toJson(this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Optional.ofNullable(obj)
                .filter(o -> obj instanceof TorrentTransportData)
                .map(o -> (TorrentTransportData) obj)
                .filter(o -> o.getId().equals(getId()))
                .filter(o -> o.seedingPeereerDto.equals(seedingPeereerDto))
                .isPresent();
    }
}
