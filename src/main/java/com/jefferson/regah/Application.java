package com.jefferson.regah;

import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        final SharedResources sharedResources = new SharedResources();
        final SharingServer sharingServer = new SharingServer(sharedResources);

        sharingServer.start();

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> sharingServer.shutdown()));
    }
}
