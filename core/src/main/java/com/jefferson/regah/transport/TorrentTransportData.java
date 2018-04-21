package com.jefferson.regah.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jefferson.regah.dto.PeerDto;
import com.jefferson.regah.transport.torrent.ByteArrayTypeAdapter;
import com.turn.ttorrent.common.Peer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class TorrentTransportData implements TransportData {
    private static final Logger log = LogManager.getLogger(TorrentTransportData.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(byte[].class, new ByteArrayTypeAdapter())
            .create();

    private final String id;
    private final PeerDto seedingPeereerDto;
    private final byte[] torrentData;

    TorrentTransportData(final String id, final Peer seedingPeer, byte[] torrentData) {
        this.id = id;
        this.seedingPeereerDto = PeerDto.fromPeer(seedingPeer);
        this.torrentData = torrentData;
    }

    Peer getSeedingPeer() throws InvalidTransportData {
        try {
            return seedingPeereerDto.toPeer();
        } catch (UnknownHostException e) {
            final String error = "Failed to create peer for remote machine, because host is unknown.";
            log.error(error, e);
            throw new InvalidTransportData(error, e);
        }
    }

    public byte[] getTorrentData() {
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
                .filter(o -> o.seedingPeereerDto.equals(seedingPeereerDto))
                .isPresent();
    }

    public static TransportData fromJson(String json) {
        return gson.fromJson(json, TorrentTransportData.class);
    }

    public static void main(String[] args) {
        final byte[] bs = "a".getBytes(StandardCharsets.UTF_8);
        Base64.getEncoder().encode(bs);

        final String json = new Gson().toJson(Map.of("bytes", bs));
        System.out.println("Bytes as json: " + json);
        Map m = new Gson().fromJson(json, Map.class);
        final Object bsFromJSon = m.get("bytes");
        System.out.println("bytes from json: " + bsFromJSon);
    }
}
