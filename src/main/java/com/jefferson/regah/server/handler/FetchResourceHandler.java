package com.jefferson.regah.server.handler;

import com.jefferson.regah.SharedResources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FetchResourceHandler implements HttpHandler {
    private static final Logger log = LogManager.getLogger(FetchResourceHandler.class);

    private final SharedResources sharedResources;

    public FetchResourceHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("Fetch resources request");
        log.debug("Headers: " + exchange.getRequestHeaders());

        // TODO: implement

        exchange.sendResponseHeaders(200, 0);
    }
}
