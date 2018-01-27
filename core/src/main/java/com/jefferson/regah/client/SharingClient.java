package com.jefferson.regah.client;

import com.jefferson.regah.SharedResources;
import com.jefferson.regah.client.handler.AddHandler;
import com.jefferson.regah.http.Server;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class SharingClient {
    private final int serverPort = 42421;
    private final SharedResources sharedResources;
    private final Server server;

    public SharingClient(SharedResources sharedResources) throws IOException {
        this.sharedResources = sharedResources;
        this.server = new Server(serverPort, "Sharing center management");
    }

    public void start() {
        final Map<String, HttpHandler> handlers = Map.of(
                "/add", new AddHandler(sharedResources));

        server.start(handlers);
    }
}