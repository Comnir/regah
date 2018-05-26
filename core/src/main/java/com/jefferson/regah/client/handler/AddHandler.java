package com.jefferson.regah.client.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.Handler;
import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.HttpExchange;
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

public class AddHandler implements Handler {
    private static final Logger log = LogManager.getLogger(AddHandler.class);
    private static final Gson gson = new Gson();

    private static final String FILE_PATHS_PARAMETER = "paths";

    private final SharedResources sharedResources;

    public AddHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public String handleHttpRequest(HttpExchange exchange) throws IOException {
        log.info("Got a request to add resources");

        // TODO: refactor out common logic between add/list handlers (will also be used for a future 'remove' handler)
        verifyRequest(exchange);

        final List<String> paths = parseRequestParameters(exchange);
        if (paths == null) {
            return "";
        }

        if (paths.isEmpty()) {
            log.warn("Add request got no paths to add.");
        } else {
            act(paths);
        }

        return "";
    }

    private List<String> parseRequestParameters(HttpExchange exchange) throws IOException {
        final String requestBody = readRequestBody(exchange);

        final Map<String, List<String>> parameters;
        try {
            parameters = gson.fromJson(requestBody,
                    TypeToken.getParameterized(Map.class, String.class, List.class).getType());
        } catch (JsonSyntaxException e) {
            throw new InvalidRequest("Failed to parse request body as JSON - expected '" + FILE_PATHS_PARAMETER + "' with a list of paths.", e);
        }
        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);

        if (null == paths) {
            throw new InvalidRequest("Error: Missing '" + FILE_PATHS_PARAMETER + "' parameter");

        }
        return paths;
    }

    private void verifyRequest(HttpExchange exchange) {
        if (!isJsonContentType(exchange)) {
            throw new InvalidRequest();
        }
    }

    private void act(final List<String> paths) {
        log.trace("Add request - got paths to add: " + paths);
        paths.stream()
                .map(File::new)
                .forEach(sharedResources::share);
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
