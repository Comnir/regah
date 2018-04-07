package com.jefferson.regah;

import com.jefferson.regah.client.SharingClient;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);
    private final int sharingServerPort;

    private final ImmutableWrapper<SharingServer> sharingServerWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<SharingClient> sharingClientWrapper = new ImmutableWrapper<>();

    private final SharedResources sharedResources;

    private Application() {
        this(42424, new SharedResources());
    }

    Application(final int sharingServerPort, final SharedResources sharedResources) {
        this.sharingServerPort = sharingServerPort;
        this.sharedResources = sharedResources;

    }

    public static void main(String[] args) {
        new Application().start();
    }

    void start() {
        boolean anySuccess = false;
        try {
            sharingServerWrapper.set(new SharingServer(sharedResources, sharingServerPort)).start();
            anySuccess = true;
        } catch (IOException e) {
            log.error("Sharing server failed to start!", e);
        }

        try {
            sharingClientWrapper.set(new SharingClient(sharedResources)).start();
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

    void stop() {
        sharingClientWrapper.asOptional().ifPresent(SharingClient::stop);
        sharingServerWrapper.asOptional().ifPresent(SharingServer::stop);
    }
}
