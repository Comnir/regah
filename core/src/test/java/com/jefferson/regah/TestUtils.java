package com.jefferson.regah;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public class TestUtils {
    private static final Logger log = LogManager.getLogger(TestUtils.class);

    public static int getAvailablePort() throws IOException {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            final int serverPort = serverSocket.getLocalPort();
            log.info("Server port: " + serverPort);
            return serverPort;
        }
    }
}
