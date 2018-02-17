package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.Responder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ListResourcesHandler implements HttpHandler {
    private static final Logger log = LogManager.getLogger(ListResourcesHandler.class);
    private static final Gson gson = new Gson();

    private final SharedResources sharedResources;
    private final Responder responder;

    public ListResourcesHandler(SharedResources sharedResources, Responder responder) {
        this.sharedResources = sharedResources;
        this.responder = responder;
    }

    @Override
    public void handle(HttpExchange exchange) {
        log.info("List resources request - method: " + exchange.getRequestMethod());

        final String response = gson.toJson(Map.of("results", sharedResources.getResources()));

        log.trace("Response: " + response);

        responder.respondeWithJson(exchange, response, 200);
    }
}
