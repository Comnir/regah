package com.jefferson.regah;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jefferson.regah.client.SharingManager;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.guice.ConfigurationModule;
import com.jefferson.regah.guice.HttpHandlersModule;
import com.jefferson.regah.notification.NotificationBus;
import com.jefferson.regah.server.SharingServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);
    private final SharingServer sharingServer;
    private final SharingManager sharingManager;

    private final ImmutableWrapper<SharingServer> sharingServerWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<SharingManager> sharingClientWrapper = new ImmutableWrapper<>();
    private final ImmutableWrapper<NotificationBus> notificationServerWrapper = new ImmutableWrapper<>();

    @Inject
    Application(final SharingServer sharingServer,
                final SharingManager sharingManager) {
        this.sharingServer = sharingServer;
        this.sharingManager = sharingManager;
    }

    public static void main(String[] args) {
        final Injector injector = Guice.createInjector(Arrays.asList(new ConfigurationModule(), new HttpHandlersModule()));
        final Application application = injector.getInstance(Application.class);

        Runtime.getRuntime().addShutdownHook(new Thread(application::stop));
        application.start();
    }

    void start() {
        sharingServerWrapper.set(sharingServer).start();
        sharingClientWrapper.set(sharingManager).start();
    }

    void stop() {
        sharingClientWrapper.asOptional().ifPresent(SharingManager::stop);
        sharingServerWrapper.asOptional().ifPresent(SharingServer::stop);
        notificationServerWrapper.asOptional().ifPresent(NotificationBus::stop);
    }
}
