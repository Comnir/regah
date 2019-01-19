package com.jefferson.regah;

import com.jefferson.regah.client.SharingManager;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.notification.NotificationBus;
import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);
    private final int sharingServerPort;

    private final ImmutableWrapper<SharingServer> sharingServerWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<SharingManager> sharingClientWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<NotificationBus> notificationServerWrapper = new ImmutableWrapper<>();

    private final SharedResources sharedResources;
    private final File parentFolderForTorrent;
    private final int notificationServerPort;

    private Application(final File applicationDataFolder) {
        this(42424, 42100, new SharedResources(), applicationDataFolder);
    }

    Application(final int sharingServerPort, int notificationServerPort, final SharedResources sharedResources, File applicationDataFolder) {
        this.sharingServerPort = sharingServerPort;
        this.sharedResources = sharedResources;

        parentFolderForTorrent = applicationDataFolder;
        this.notificationServerPort = notificationServerPort;
    }

    public static void main(String[] args) {
        final File dataFolder = getDataFolder();
        final Application application = new Application(dataFolder);
        Runtime.getRuntime().addShutdownHook(new Thread(application::stop));
        application.start();
    }

    private static File getDataFolder() {
        final File dataFolder = Paths.get(System.getProperty("user.home"), "regah").toFile();
        if (!dataFolder.exists() && !dataFolder.mkdir()) {
            final String message = String.format("Data folder doesn't exist and could not be created. Path: %s", dataFolder);
            log.fatal(message);
            throw new IllegalStateException(message);
        }
        return dataFolder;
    }

    void start() {
        try {
            sharingServerWrapper.set(new SharingServer(sharedResources, sharingServerPort, parentFolderForTorrent)).start();
        } catch (IOException e) {
            logAndExit(e, "Sharing server failed to start!");
        }

        try {
            sharingClientWrapper.set(new SharingManager(sharedResources)).start();
        } catch (IOException e) {
            logAndExit(e, "Sharing client failed to start!");
        }

        NotificationBus.startOnPort(notificationServerPort);
    }

    private void logAndExit(IOException e, String s) {
        log.error(s, e);
        System.exit(-1);
    }

    void stop() {
        sharingClientWrapper.asOptional().ifPresent(SharingManager::stop);
        sharingServerWrapper.asOptional().ifPresent(SharingServer::stop);
        notificationServerWrapper.asOptional().ifPresent(NotificationBus::stop);
    }
}
