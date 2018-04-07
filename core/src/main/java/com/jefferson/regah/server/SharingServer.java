package com.jefferson.regah.server;

import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.ErrorWrappingHandler;
import com.jefferson.regah.handler.Responder;
import com.jefferson.regah.http.Server;
import com.jefferson.regah.server.handler.FetchResourceHandler;
import com.jefferson.regah.server.handler.ListResourcesHandler;
import com.jefferson.regah.transport.TorrentTransporter;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * A sharing server provides the ability to list shared files and folders
 * and download them.
 */
public class SharingServer {
    private static final Logger log = LogManager.getLogger(SharingServer.class);
    private final SharedResources sharedResources;
    private final Server server;

    public SharingServer(SharedResources sharedResources, int serverPort) throws IOException {
        this.sharedResources = sharedResources;
        server = new Server(serverPort, "Sharing center server");
    }

    public void start() throws IOException {
        final Map<String, HttpHandler> handlers = Map.of(
                "/listShared", new ErrorWrappingHandler(new ListResourcesHandler(sharedResources, new Responder())),
                "/fetchResources", new ErrorWrappingHandler(new FetchResourceHandler(sharedResources,
                        new TorrentTransporter(), new Responder())));

        server.start(handlers);
    }

    public void stop() {
        server.stop();
    }
}
