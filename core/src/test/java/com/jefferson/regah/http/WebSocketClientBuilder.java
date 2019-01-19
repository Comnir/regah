package com.jefferson.regah.http;

import com.jefferson.regah.util.TriConsumer;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class WebSocketClientBuilder {
    private URI serverUri;
    private Consumer<ServerHandshake> onOpenDelegate = System.out::println;
    private Consumer<String> onMessageDelegate = System.out::println;
    private TriConsumer<Integer, String, Boolean> onCloseDelegate = (code, reason, remote) -> System.out.println("Closed with code " + code + ", reason: " + reason + " remote? " + remote);
    private Consumer<Exception> onErrorDelegate = System.err::println;

    public WebSocketClientBuilder setServerUri(URI serverUri) {
        this.serverUri = serverUri;
        return this;
    }

    public WebSocketClientBuilder setOnOpenDelegate(Consumer<ServerHandshake> onOpenDelegate) {
        this.onOpenDelegate = onOpenDelegate;
        return this;
    }

    public WebSocketClientBuilder setOnMessageDelegate(Consumer<String> onMessageDelegate) {
        this.onMessageDelegate = onMessageDelegate;
        return this;
    }

    public WebSocketClientBuilder setOnCloseDelegate(TriConsumer<Integer, String, Boolean> onCloseDelegate) {
        this.onCloseDelegate = onCloseDelegate;
        return this;
    }

    public WebSocketClientBuilder setOnErrorDelegate(Consumer<Exception> onErrorDelegate) {
        this.onErrorDelegate = onErrorDelegate;
        return this;
    }

    public WebSockerClient createNotificationClient() {
        return new WebSockerClient(serverUri, onOpenDelegate, onMessageDelegate, onCloseDelegate, onErrorDelegate);
    }
}