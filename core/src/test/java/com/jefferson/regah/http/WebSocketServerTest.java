package com.jefferson.regah.http;

import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class WebSocketServerTest {
    private static final int SERVER_PORT = 42000;
    private final ImmutableWrapper<WebSocketServer> serverWrapper = new ImmutableWrapper<>();

    @BeforeEach
    void setup() {
        final InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), SERVER_PORT);
        serverWrapper.set(new WebSocketServerBuilder().setAddress(address).createWebSocketServer());
        serverWrapper.get().start();


    }

    @AfterEach
    void teardown() throws IOException, InterruptedException {
        if (serverWrapper.isPresent()) {
            serverWrapper.get().stop();
        }
    }

    @Test
    void test() throws URISyntaxException, InterruptedException {
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        final WebSocketClient client = new WebSocketClientBuilder().setServerUri(new URI("ws://127.0.0.1:42000"))
                .setOnMessageDelegate(queue::add
                ).createNotificationClient();


        final String expectedMessage = "sending forEach";
        assertTimeoutPreemptively(Duration.ofSeconds(1), (ThrowingSupplier<Boolean>) client::connectBlocking);
        serverWrapper.get().getConnections().forEach(s -> s.send(expectedMessage));

        assertEquals(expectedMessage, queue.poll(2, TimeUnit.SECONDS));

        // closing the client causes issues with closing the socket.
        // on linux, socket will go to TIME_WAIT state for ~1 minute.
    }
}