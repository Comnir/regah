package com.jefferson.regah.client;

import com.google.inject.Inject;
import com.jefferson.regah.http.Server;
import com.sun.net.httpserver.HttpHandler;

import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

public class SharingManager {
    private final int serverPort = 42421;
    private final Server server;
    private final HttpHandler addHandler;
    private final HttpHandler downloadHandler;

    @Inject
    public SharingManager(@Named("addHandler") final HttpHandler addHandler,
                          @Named("downloadHandler") final HttpHandler downloadHandler) throws IOException {
        this.addHandler = addHandler;
        this.downloadHandler = downloadHandler;
        this.server = new Server(serverPort, "Sharing center management"); // TODO: inject
    }

    public void start() {
        final Map<String, HttpHandler> handlers = Map.of(
                "/add", addHandler,
                "/download", downloadHandler);

        server.start(handlers);
    }

    public void stop() {
        server.stop();
    }
}