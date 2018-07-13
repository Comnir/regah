package com.jefferson.regah.dto;

import com.turn.ttorrent.common.Peer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeerDtoTest {
    @Test
    void dtosEqualWhenCreatedFromTheSamePeer() {
        final Peer peer = new Peer("192.168.2.4", 54321);
        assertEquals(PeerDto.fromPeer(peer), PeerDto.fromPeer(peer),
                "DTOs should be equal if created from the same peer.");
    }

    @Test
    void dtosEqualWhenCreatedFromPeerWithTheSameIpAndPort() {
        final String ip = "1.1.1.1";
        final int port = 2222;
        final Peer peer1 = new Peer(ip, port);
        final Peer peer2 = new Peer(ip, port);
        assertEquals(PeerDto.fromPeer(peer1), PeerDto.fromPeer(peer2),
                "DTOs should be equal if created from peers that have the same IP and port.");
    }
}