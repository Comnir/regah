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
import java.util.Optional;

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

        // TODO: refactor out common logic between add/list handlers (will also be used for a future 'remove' handler)
        if (!isJsonContentType(exchange)) {
            final String error = "Invalid request format!";
            log.error(error);
            final String responseJson = gson.toJson(Map.of(
                    HttpConstants.ERROR_REASON,
                    error));

            responder.respondeWithJson(exchange, responseJson, 400);
            return;
        }

        final String requestBody = readRequestBody(exchange);

        final Map<String, List<String>> parameters;
        try {
            parameters = gson.fromJson(requestBody,
                    TypeToken.getParameterized(Map.class, String.class, List.class).getType());
        } catch (JsonSyntaxException e) {
            final String error = "Failed to parse request body as JSON - expected '" + FILE_PATHS_PARAMETER + "' with a list of paths.";
            log.error(error);
            responder.respondeWithJson(exchange, error, 400);
            return;
        }
        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);

        if (null == paths) {
            final String error = "Error: Missing '" + FILE_PATHS_PARAMETER + "' parameter";
            log.error(error);
            responder.respondeWithJson(exchange, error, 400);
            return;
        }

        if (!paths.isEmpty()) {
            log.trace("Add request - got paths to add: " + paths);
            paths.stream()
                    .map(File::new)
                    .forEach(sharedResources::share);
        } else {
            log.warn("Add request got no paths to add.");
        }

        responder.respondeWithJson(exchange, "", 200);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        try (final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            stringBuilder.append(bufferedReader.readLine());
        }

        final String requestBody = stringBuilder.toString();
        log.trace(String.format("Add request - request body: %s", requestBody));
        return requestBody;
    }

    private boolean isJsonContentType(HttpExchange exchange) {
        return Optional
                .ofNullable(exchange.getRequestHeaders().getFirst(HttpConstants.CONTENT_TYPE))
                .filter(type -> type.startsWith(HttpConstants.APPLICATION_JSON))
                .isPresent();
    }
}
