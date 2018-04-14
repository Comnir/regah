package com.jefferson.regah;

import com.jefferson.regah.client.SharingManager;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);
    private final int sharingServerPort;

    private final ImmutableWrapper<SharingServer> sharingServerWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<SharingManager> sharingClientWrapper = new ImmutableWrapper<>();

    private final SharedResources sharedResources;
    private final File parentFolderForTorrent;

    private Application(final File applicationDataFolder) {
        this(42424, new SharedResources(), applicationDataFolder);
    }

    Application(final int sharingServerPort, final SharedResources sharedResources, File applicationDataFolder) {
        this.sharingServerPort = sharingServerPort;
        this.sharedResources = sharedResources;

        parentFolderForTorrent = applicationDataFolder;
    }

    public static void main(String[] args) throws IOException {
        final File dataFolder = Files.createDirectory(Paths.get("")).toFile();

        new Application(dataFolder).start();
    }

    void start() {
        boolean anySuccess = false;
        try {
            sharingServerWrapper.set(new SharingServer(sharedResources, sharingServerPort, parentFolderForTorrent)).start();
            anySuccess = true;
        } catch (IOException e) {
            log.error("Sharing server failed to start!", e);
        }

        try {
            sharingClientWrapper.set(new SharingManager(sharedResources)).start();
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
        sharingClientWrapper.asOptional().ifPresent(SharingManager::stop);
        sharingServerWrapper.asOptional().ifPresent(SharingServer::stop);
    }
}
