package com.jefferson.regah.http;

import com.jefferson.regah.util.QuadConsumer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

public class WebSocketServerBuilder {
    private InetSocketAddress address;
    private BiConsumer<WebSocket, ClientHandshake> onOpenDelegate;
    private BiConsumer<WebSocket, String> onMessageDelegate;
    private QuadConsumer<WebSocket, Integer, String, Boolean> onCloseDelegate;
    private BiConsumer<WebSocket, Exception> onErrorDelegate;
    private Runnable onStartDelegate;

    public WebSocketServerBuilder setAddress(InetSocketAddress address) {
        this.address = address;
        return this;
    }

    public WebSocketServerBuilder setOnOpenDelegate(BiConsumer<WebSocket, ClientHandshake> onOpenDelegate) {
        this.onOpenDelegate = onOpenDelegate;
        return this;
    }

    public WebSocketServerBuilder setOnMessageDelegate(BiConsumer<WebSocket, String> onMessageDelegate) {
        this.onMessageDelegate = onMessageDelegate;
        return this;
    }

    public WebSocketServerBuilder setOnCloseDelegate(QuadConsumer<WebSocket, Integer, String, Boolean> onCloseDelegate) {
        this.onCloseDelegate = onCloseDelegate;
        return this;
    }

    public WebSocketServerBuilder setOnErrorDelegate(BiConsumer<WebSocket, Exception> onErrorDelegate) {
        this.onErrorDelegate = onErrorDelegate;
        return this;
    }

    public WebSocketServerBuilder setOnStartDelegate(Runnable onStartDelegate) {
        this.onStartDelegate = onStartDelegate;
        return this;
    }

    public WebSocketServer createWebSocketServer() {
        return new WebSocketServer(address, onOpenDelegate, onMessageDelegate, onCloseDelegate, onErrorDelegate, onStartDelegate);
    }
}