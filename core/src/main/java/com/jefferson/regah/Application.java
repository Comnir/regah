package com.jefferson.regah;

import com.jefferson.regah.client.SharingClient;
import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        boolean anySuccess = false;
        final SharedResources sharedResources = new SharedResources();
        try {
            new SharingServer(sharedResources).start();
            anySuccess = true;
        } catch (IOException e) {
            log.error("Sharing server failed to start!", e);
        }

        try {
            new SharingClient(sharedResources).start();
            anySuccess = true;
        } catch (IOException e) {
            log.error("Sharing client failed to start!", e);
        }

        if (!anySuccess) {
            log.fatal("Failed to start any service during startup of the application."
                    + System.lineSeparator()
                    + "The application will exit.");
            System.exit(-1);
        }
    }
}
