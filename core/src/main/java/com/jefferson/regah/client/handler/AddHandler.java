package com.jefferson.regah.client.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.Responder;
import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AddHandler implements HttpHandler {
    private static final Logger log = LogManager.getLogger(AddHandler.class);
    private static final Gson gson = new Gson();

    private static final String FILE_PATHS_PARAMETER = "paths";

    private final SharedResources sharedResources;
    private final Responder responder;

    public AddHandler(SharedResources sharedResources, Responder responder) {
        this.sharedResources = sharedResources;
        this.responder = responder;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("Got a request to add resources");

        if (!HttpConstants.APPLICATION_JSON.equals(exchange.getRequestHeaders().getFirst(HttpConstants.CONTENT_TYPE))) {
            final String responseJson = gson.toJson(Map.of(
                    HttpConstants.ERROR_REASON,
                    "Invalid request format!"));

            responder.sendResponse(exchange, responseJson, 400);
            return;
        }

        final StringBuilder stringBuilder = new StringBuilder();
        try (final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            stringBuilder.append(bufferedReader.readLine());
        }

        final Map<String, List<String>> parameters;
        try {
            parameters = gson.fromJson(stringBuilder.toString(),
                    TypeToken.getParameterized(Map.class, String.class, List.class).getType());
        } catch (JsonSyntaxException e) {
            responder.sendResponse(exchange, "Failed to parse request body as JSON - expected '" + FILE_PATHS_PARAMETER + "' with a list of paths.", 400);
            return;
        }
        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);

        if (null == paths) {
            responder.sendResponse(exchange, "Error: Missing '" + FILE_PATHS_PARAMETER + "' parameter", 400);
            return;
        }

        if (!paths.isEmpty()) {
            paths.stream()
                    .map(File::new)
                    .forEach(sharedResources::share);
        }

        responder.sendResponse(exchange, "", 200);
    }
}
