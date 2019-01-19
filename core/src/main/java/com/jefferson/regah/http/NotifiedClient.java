package com.jefferson.regah.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class NotifiedClient extends WebSocketClient {
    private static final Logger log = LogManager.getLogger(NotifiedClient.class);

    public NotifiedClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("opened connection {}", handshakedata);
    }

    @Override
    public void onMessage(String message) {
        log.info("get message from server '{}'", message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("connection with server was closed by {} - code: {} reason: '{}'", remote ? "server":"client(us)", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error("error on connection with server", ex);
    }

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        final NotifiedClient notifiedClient = new NotifiedClient(new URI("ws://localhost:42100"));

        if (notifiedClient.connectBlocking()) {
            log.info("successful connection to server");
        } else {
            log.warn("failed to connect to server");
        }
    }
}
