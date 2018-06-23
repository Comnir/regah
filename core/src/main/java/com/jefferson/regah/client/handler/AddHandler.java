package com.jefferson.regah.client.handler;

import com.google.gson.Gson;
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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddHandler implements Handler<Map<String, List<String>>> {
    private static final Logger log = LogManager.getLogger(AddHandler.class);
    private static final Gson gson = new Gson();

    private static final String FILE_PATHS_PARAMETER = "paths";

    private final SharedResources sharedResources;

    public AddHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public String handleHttpRequest(HttpExchange exchange) {
        log.info("Got a request to add resources");

        // TODO: refactor out common logic between add/list handlers (will also be used for a future 'remove' handler)
        verifyRequest(exchange);

        final Map<String, List<String>> parameters = parseRequestParameters(exchange);

        return act(parameters);
    }

    private Map<String, List<String>> parseRequestParameters(HttpExchange exchange) {
        throw new IllegalAccessError("Should not get here!");
//        final String requestBody = readRequestBody(exchange);
//        final Optional<Type> type = typeForJsonParsing();
//        final Map<String, List<String>> parameters;
//        if (!type.isPresent()) {
//            return Collections.emptyMap();
//        }
//        try {
//            parameters = gson.fromJson(requestBody, type.get());
//        } catch (JsonSyntaxException ex) {
//            throw new InvalidRequest("Failed to parse JSON to type " + type);
//        }
//        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);
//
//        if (null == paths) {
//            throw new InvalidRequest("Error: Missing '" + FILE_PATHS_PARAMETER + "' parameter");
//        }
    }

    @Override
    public Optional<Type> typeForJsonParsing() {
        return Optional.of(TypeToken.getParameterized(Map.class, String.class, List.class).getType());
    }

    private void verifyRequest(HttpExchange exchange) {
        if (!isJsonContentType(exchange)) {
            throw new InvalidRequest();
        }
    }

    private boolean isJsonContentType(HttpExchange exchange) {
        return Optional
                .ofNullable(exchange.getRequestHeaders().getFirst(HttpConstants.CONTENT_TYPE))
                .filter(type -> type.startsWith(HttpConstants.APPLICATION_JSON))
                .isPresent();
    }

    @Override
    public String act(final Map<String, List<String>> parameters) {
        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);

        if (null == paths) {
            throw new InvalidRequest("Error: Missing '" + FILE_PATHS_PARAMETER + "' parameter");
        }

        if (paths.isEmpty()) {
            log.warn("Add request got no paths to add.");
        } else {
            log.trace("Add request - got paths to add: " + paths);
            paths.stream()
                    .map(File::new)
                    .forEach(sharedResources::share);
        }

        return "";
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
}
