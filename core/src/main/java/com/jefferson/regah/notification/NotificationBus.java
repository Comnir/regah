package com.jefferson.regah.notification;

import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.http.WebSocketServer;
import com.jefferson.regah.http.WebSocketServerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NotificationBus implements NotificationSender {
    private static final Logger log = LogManager.getLogger(NotificationBus.class);

    private static final String SUBSCRIPTION_PREFIX = "/subscribe/";

    private final WebSocketServer server;
    private final Map<String, WebSocket> subscribers;
    private final CountDownLatch startedLatch;
    private final ImmutableWrapper<Exception> startFailureWrapper = new ImmutableWrapper<>();

    public static void startOnPort(int notificationServerPort) {
        InstanceHolder.instance.set(
                new NotificationBus(new InetSocketAddress(InetAddress.getLoopbackAddress(), notificationServerPort)))
                .start();
    }

    private static class InstanceHolder {
        private static final ImmutableWrapper<NotificationBus> instance = new ImmutableWrapper<>();
    }

    public static NotificationBus getInstance() {
        return InstanceHolder.instance.get();
    }

    NotificationBus(InetSocketAddress address) {
        server = new WebSocketServerBuilder()
                .setAddress(address)
                .setOnOpenDelegate(this::onOpen)
                .setOnMessageDelegate(this::onMessage)
                .setOnErrorDelegate(this::onError)
                .setOnCloseDelegate(this::onClose)
                .setOnStartDelegate(this::onStart)
                .createWebSocketServer();
        subscribers = new ConcurrentHashMap<>();
        startedLatch = new CountDownLatch(1);
    }

    private void onStart() {
        startedLatch.countDown();
    }

    public void start() {
        server.start();
        server.setConnectionLostTimeout(100);
    }

    /**
     * Waits for the bus to be ready.
     *
     * @param timeout to wait for the bus to be ready.
     * @return true if the bus is ready.
     */
    public boolean waitForStart(Duration timeout) {
        try {
            if (!startedLatch.await(timeout.toMillis(), TimeUnit.SECONDS)) {
                return false;
            }

            if (startFailureWrapper.isPresent()) {
                log.error("Waited for notification bus to start, but it failed to start with an exception.", startFailureWrapper.get());
                throw new RuntimeException(startFailureWrapper.get());
            }

            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void stop() {
        try {
            stop(2000);
        } catch (InterruptedException e) {
            log.warn("Notification bus stopping was interrupted.", e);
        }
    }

    public void stop(int timeout) throws InterruptedException {
        server.stop(timeout);
        Thread.sleep(1000);
    }

    private void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info("Web socket opened '{}' handshake: {}", webSocket.getResourceDescriptor(), clientHandshake.getResourceDescriptor());

        Optional.of(webSocket)
                .map(WebSocket::getResourceDescriptor)
                .filter(d -> d.startsWith(SUBSCRIPTION_PREFIX))
                .map(d -> d.substring(SUBSCRIPTION_PREFIX.length()))
                .ifPresent(id -> subscribers.put(id, webSocket));

        log.debug("Subscriber IDs: {}", subscribers.keySet());
    }

    private void onMessage(WebSocket webSocket, String message) {
        log.info("Message from web socket {}: {}", webSocket, message);
    }

    private void onClose(WebSocket webSocket, Integer code, String reason, Boolean remote) {
        log.info("Web socket {} closed with code {}, reason '{}' by {}", webSocket, code, reason, remote ? "remote" : "us");
    }

    private void onError(WebSocket webSocket, Exception e) {
        log.error("Web socket {} encountered error: {}", webSocket, e);
        if (1 == startedLatch.getCount()) {
            synchronized (startFailureWrapper) {
                log.error("Failed to start notification bus.");
                startFailureWrapper.set(e);
            }
            startedLatch.countDown();
        }
    }

    @Override
    public boolean sendMessageTo(String subscriptionId, String message) {
        final WebSocket subscriber = subscribers.get(subscriptionId);
        if (null != subscriber) {
            log.debug("sending message '{}' to {}", subscriptionId, message);
            subscriber.send(message);
            return true;
        }
        return false;
    }
}
