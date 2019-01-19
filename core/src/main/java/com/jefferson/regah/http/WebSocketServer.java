package com.jefferson.regah.http;

import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.util.QuadConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BiConsumer;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
    private static final Logger log = LogManager.getLogger(WebSocketServer.class);

    private final BiConsumer<WebSocket, ClientHandshake> onOpenDelegate;
    private final BiConsumer<WebSocket, String> onMessageDelegate;
    private final QuadConsumer<WebSocket, Integer, String, Boolean> onCloseDelegate;
    private final BiConsumer<WebSocket, Exception> onErrorDelegate;
    private final ImmutableWrapper<Boolean> started = new ImmutableWrapper<>();
    private final Runnable onStartDelegate;

    WebSocketServer(InetSocketAddress address, BiConsumer<WebSocket, ClientHandshake> onOpenDelegate,
                    BiConsumer<WebSocket, String> onMessageDelegate, QuadConsumer<WebSocket, Integer, String, Boolean> onCloseDelegate,
                    BiConsumer<WebSocket, Exception> onErrorDelegate, Runnable onStartDelegate) {
        super(address);
        this.onOpenDelegate = Optional.ofNullable(onOpenDelegate).orElse(this::defaultOnOpen);
        this.onMessageDelegate = Optional.ofNullable(onMessageDelegate).orElse(this::defaultOnMessage);
        this.onCloseDelegate = Optional.ofNullable(onCloseDelegate).orElse(this::defaultOnClose);
        this.onErrorDelegate = Optional.ofNullable(onErrorDelegate).orElse(this::defaultOnError);
        this.onStartDelegate = Optional.ofNullable(onStartDelegate).orElse(this::defaultOnStart);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        onOpenDelegate.accept(conn, handshake);
    }

    private void defaultOnOpen(WebSocket conn, ClientHandshake handshake) {
        log.info("Web socket connection opened: {}, handshake descriptor: {}", conn, handshake.getResourceDescriptor());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        onCloseDelegate.accept(conn, code, reason, remote);
    }

    private void defaultOnClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Web socket connection closed {}. Code {} with reason: {}. Closed by {}.", conn, code, reason, remote ? "remote" : "host");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        onMessageDelegate.accept(conn, message);
    }

    private void defaultOnMessage(WebSocket conn, String message) {
        log.debug("Web socket connection {} received message '{}'", conn, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        onErrorDelegate.accept(conn, ex);
    }

    private void defaultOnError(WebSocket conn, Exception ex) {
        log.error("Error on web socket conn {} - {}", conn, ex);
    }

    @Override
    public void onStart() {
        onStartDelegate.run();
    }

    private void defaultOnStart() {
        log.info("Websocket server has started successfully.");
    }

    public void waitTillStarted(Duration timeout) {
        synchronized (started) {
            if (started.isEmpty()) {
                try {
                    started.wait(timeout.toMillis());
                } catch (InterruptedException e) {
                    log.warn("Interrupted while waiting for web socket server to start.");

                }
            }
        }
    }
}
