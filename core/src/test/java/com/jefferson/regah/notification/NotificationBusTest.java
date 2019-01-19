package com.jefferson.regah.notification;

import com.jefferson.regah.http.WebSocketClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NotificationBusTest {
    private static final Logger log = LogManager.getLogger(NotificationBusTest.class);
    private static final int SERVER_PORT = 42000;

    private final InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), SERVER_PORT);
    private NotificationBus notificationBus;

    @BeforeEach
    void setup() {
        notificationBus = new NotificationBus(address);
        notificationBus.start(); // fails asynchronously about every other time
        assertTrue(notificationBus.waitForStart(Duration.ofSeconds(3)), "Timed out waiting for notification bus.");
    }

    @AfterEach
    void teardown() throws InterruptedException {
        if (null != notificationBus) {
            log.info("Stopping notification bus");
            notificationBus.stop(5);
            log.info("Stopping notification bus - done");
        }
    }

    @Test
    void sendMessageReturnsFalseWhenSubscirberIdDoesntExist() {
        assertFalse(notificationBus.sendMessageTo("does_not_exist", "any"));
    }

    @Test
    void subscriberRecievsMessage() throws URISyntaxException, InterruptedException {
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        final String subscriptionId = "SOME_ID";
        final WebSocketClient client = new WebSocketClientBuilder().setServerUri(new URI("ws://127.0.0.1:" + SERVER_PORT + "/subscribe/" + subscriptionId))
                .setOnMessageDelegate(queue::add
                ).createNotificationClient();


        final String expectedMessage = "message to subscriber";
        assertTimeoutPreemptively(Duration.ofSeconds(1), (ThrowingSupplier<Boolean>) client::connectBlocking);
        // the client might not have been subscribed yet

        for (int i = 0; i < 5; i++) {
            if (!notificationBus.sendMessageTo(subscriptionId, expectedMessage)) {
                // subscription might have not been registered yet
                Thread.sleep(1000);
            } else {
                break;
            }
        }

        assertEquals(expectedMessage, queue.poll(1, TimeUnit.SECONDS));

        // if closeBlocking is called on client, the connection with the server goes to "TIME_WAIT" state (on linux)
        // and other tests will fail when starting the server
    }
}