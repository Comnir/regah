package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class ListResourcesHandler implements HttpHandler {
    private static final Logger log = LogManager.getLogger(ListResourcesHandler.class);
    private static final Gson gson = new Gson();

    private final SharedResources sharedResources;

    public ListResourcesHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("List resources request");
        log.debug("Headers: " + exchange.getRequestHeaders());

        final String response = gson.toJson(sharedResources.getResources().stream().collect(Collectors.toList()));
        log.debug("Return shared resources: " + response);

        exchange.getResponseHeaders().add("Content-type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        } catch (Exception e) {
            log.error("Error while writing response", e);
        }
    }
}
