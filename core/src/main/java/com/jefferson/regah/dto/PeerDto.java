package com.jefferson.regah.dto;

import com.google.gson.Gson;
import com.turn.ttorrent.common.Peer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class PeerDto {
    private static final Gson gson = new Gson();
    private final byte[] ip;
    private final int port;

    private PeerDto(byte[] ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static PeerDto fromPeer(final Peer peer) {
        return new PeerDto(peer.getRawIp(), peer.getPort());
    }

    public Peer toPeer() throws UnknownHostException {
        final InetAddress inetAddress = InetAddress.getByAddress(ip);
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
        return new Peer(inetSocketAddress);
    }

    public static String peerToJson(final Peer peer) {
        return gson.toJson(fromPeer(peer));
    }

    public static Peer jsonToPeer(final String json) {
        final PeerDto dto = gson.fromJson(json, PeerDto.class);
        try {
            final InetAddress inetAddress = InetAddress.getByAddress(dto.ip);
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, dto.port);
            return new Peer(inetSocketAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
