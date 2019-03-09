package com.jefferson.regah;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jefferson.regah.client.SharingManager;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.guice.ApplicationModule;
import com.jefferson.regah.notification.NotificationBus;
import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);
    private final int sharingServerPort;
    private final int sharingClientPort;

    private final ImmutableWrapper<SharingServer> sharingServerWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<SharingManager> sharingClientWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<NotificationBus> notificationServerWrapper = new ImmutableWrapper<>();

    private final SharedResources sharedResources;
    private final File parentFolderForTorrent;
    private final int notificationServerPort;

    @Inject
    Application(@Named("sharing-server-port") final String sharingServerPort,
                @Named("sharing-client-port") final String sharingClientPort,
                @Named("notification-server-port") final String notificationServerPort,
                final SharedResources sharedResources,
                @Named("application-data-folder") final String applicationDataFolder) {
        this(Integer.parseInt(sharingServerPort), Integer.parseInt(sharingClientPort), Integer.parseInt(notificationServerPort), sharedResources, new File(applicationDataFolder));
    }

    Application(final int sharingServerPort,
                final int sharingClientPort,
                final int notificationServerPort,
                final SharedResources sharedResources,
                final File applicationDataFolder) {
        this.sharingServerPort = sharingServerPort;
        this.sharingClientPort = sharingClientPort;
        this.sharedResources = sharedResources;
        parentFolderForTorrent = applicationDataFolder;
        this.notificationServerPort = notificationServerPort;
    }

    public static void main(String[] args) {
        final Injector injector = Guice.createInjector(new ApplicationModule());
        final Application application = injector.getInstance(Application.class);

        final File dataFolder = getDataFolder();
//        final Application application = new Application(config, dataFolder);
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
