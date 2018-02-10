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
        log.info("List resources request");

        if (!HttpConstants.APPLICATION_JSON.equals(exchange.getRequestHeaders().getFirst(HttpConstants.CONTENT_TYPE))) {
            final String responseJson = gson.toJson(Map.of(
                    HttpConstants.ERROR_REASON,
                    "Invalid request format!"));

            responder.sendResponse(exchange, responseJson, 400);
            return;
        }

        final String response = gson.toJson(Map.of("results", sharedResources.getResources()));

        responder.sendResponse(exchange, response, 200);
    }
}
