package com.jefferson.regah.server;

import com.jefferson.regah.http.Server;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

/**
 * A sharing server provides the ability to list shared files and folders
 * and download them.
 */
public class SharingServer {
    private static final Logger log = LogManager.getLogger(SharingServer.class);
    private final Server server;
    private final HttpHandler prepareResourceForDownloadHandler;
    private final HttpHandler listResourcesHandler;

    @Inject
    public SharingServer(@Named("sharing-server-port") int serverPort,
                         @Named("prepareResourceForDownload") HttpHandler prepareResourceForDownloadHandler,
                         @Named("addResource") HttpHandler listResourcesHandler) throws IOException {
        server = new Server(serverPort, "Sharing center server"); // TODO: inject
        this.prepareResourceForDownloadHandler = prepareResourceForDownloadHandler;
        this.listResourcesHandler = listResourcesHandler;
    }

    public void start() {
        final Map<String, HttpHandler> handlers = Map.of(
                "/listShared", listResourcesHandler,
                "/prepareResourceForDownload", prepareResourceForDownloadHandler);

        server.start(handlers);
    }

    public void stop() {
        server.stop();
    }
}
