package com.jefferson.regah;

import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        final SharedResources sharedResources = new SharedResources();
        final SharingServer sharingServer = new SharingServer(sharedResources);
        try {
            sharingServer.start();
            log.info("Sharing server was started.");
        } catch (IOException e) {
            log.error("Sharing server failed to start!", e);
        }
    }
}
