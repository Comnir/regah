package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.Handler;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class ListResourcesHandler implements Handler {
    private static final Logger log = LogManager.getLogger(ListResourcesHandler.class);
    private static final Gson gson = new Gson();

    private final SharedResources sharedResources;

    public ListResourcesHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public String handleHttpRequest(HttpExchange exchange) {
        log.info("List resources request - method: " + exchange.getRequestMethod());

        final String response = gson.toJson(Map.of("results", sharedResources.getResources()));

        log.trace("Response: " + response);

        return response;
    }

    @Override
    public Optional<Type> typeForJsonParsing() {
        return Optional.empty();
    }

    @Override
    public String act(Object parameters) {
        final String response = gson.toJson(Map.of("results", sharedResources.getResources()));
        return response;
    }
}
