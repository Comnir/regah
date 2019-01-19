package com.jefferson.regah.http;

import com.jefferson.regah.util.TriConsumer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class WebSockerClient extends WebSocketClient {
    private final Consumer<ServerHandshake> onOpenDelegate;
    private final Consumer<String> onMessageDelegate;
    private final TriConsumer<Integer, String, Boolean> onCloseDelegate;
    private final Consumer<Exception> onErrorDelegate;

    WebSockerClient(URI serverUri, Consumer<ServerHandshake> onOpenDelegate, Consumer<String> onMessageDelegate,
                    TriConsumer<Integer, String, Boolean> onCloseDelegate, Consumer<Exception> onErrorDelegate) {
        super(serverUri);
        this.onOpenDelegate = onOpenDelegate;
        this.onMessageDelegate = onMessageDelegate;
        this.onCloseDelegate = onCloseDelegate;
        this.onErrorDelegate = onErrorDelegate;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        onOpenDelegate.accept(handshakedata);
    }

    @Override
    public void onMessage(String message) {
        onMessageDelegate.accept(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        onCloseDelegate.accept(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        onErrorDelegate.accept(ex);
    }
}
