package com.jefferson.regah;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jefferson.regah.client.SharingManager;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.guice.ApplicationModule;
import com.jefferson.regah.guice.HttpHandlersModule;
import com.jefferson.regah.notification.NotificationBus;
import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);
    private final SharingServer sharingServer;
    private final SharingManager sharingManager;

    private final ImmutableWrapper<SharingServer> sharingServerWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<SharingManager> sharingClientWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<NotificationBus> notificationServerWrapper = new ImmutableWrapper<>();

    private final File parentFolderForTorrent;
    private final int notificationServerPort;

    @Inject
    Application(final SharingServer sharingServer,
                final SharingManager sharingManager,
                @Named("notification-server-port") final int notificationServerPort,
                final SharedResources sharedResources,
                @Named("application-data-folder") final String applicationDataFolder) {
        this.sharingServer = sharingServer;
        this.sharingManager = sharingManager;
        parentFolderForTorrent = new File(applicationDataFolder);
        this.notificationServerPort = notificationServerPort;
    }

    public static void main(String[] args) {
        final Injector injector = Guice.createInjector(Arrays.asList(new ApplicationModule(), new HttpHandlersModule()));
        final Application application = injector.getInstance(Application.class);

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
        sharingServerWrapper.set(sharingServer).start();
        sharingClientWrapper.set(sharingManager).start();
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
