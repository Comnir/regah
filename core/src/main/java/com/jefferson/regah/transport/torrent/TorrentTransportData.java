package com.jefferson.regah.transport.torrent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.jefferson.regah.dto.PeerDto;
import com.jefferson.regah.transport.InvalidTransportData;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.TransportDataVisitor;
import com.turn.ttorrent.common.Peer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;

public class TorrentTransportData implements TransportData {
    static final String ID = "id";
    static final String SEEDING_PEER_DTO = "seedingPeerDto";
    static final String TORRENT_DATA = "torrentData";

    private static final Logger log = LogManager.getLogger(TorrentTransportData.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(byte[].class, new ByteArrayTypeAdapter())
            .create();

    @SerializedName(value = ID)
    private final String id;
    @SerializedName(value = SEEDING_PEER_DTO)
    private final PeerDto seedingPeerDto;
    @SerializedName(value = TORRENT_DATA)
    private final byte[] torrentData;

    TorrentTransportData(final String id, final Peer seedingPeer, byte[] torrentData) {
        this.id = id;
        this.seedingPeerDto = PeerDto.fromPeer(seedingPeer);
        this.torrentData = torrentData;
    }

    Peer getSeedingPeer() throws InvalidTransportData {
        try {
            return seedingPeerDto.toPeer();
        } catch (UnknownHostException e) {
            final String error = "Failed to create peer for remote machine, because host is unknown.";
            log.error(error, e);
            throw new InvalidTransportData(error, e);
        }
    }

    byte[] getTorrentData() {
        return torrentData;
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
    public void acceptVisitor(TransportDataVisitor visitor) {
        visitor.visit(this);
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
                .filter(o -> o.seedingPeerDto.equals(seedingPeerDto))
                .isPresent();
    }

    public static TransportData fromJson(String json) {
        final TorrentTransportData data = gson.fromJson(json, TorrentTransportData.class);
        Objects.requireNonNull(data.getId());
        Objects.requireNonNull(data.getSeedingPeer());
        Objects.requireNonNull(data.getTorrentData());
        return data;
    }
}
