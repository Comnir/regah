package com.jefferson.regah.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jefferson.regah.client.handler.InvalidRequest;
import com.jefferson.regah.handler.Handler;
import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BaseHandler<T> {
    private static final Logger log = LogManager.getLogger(BaseHandler.class);
    private static final Gson gson = new Gson();
    private final Handler<T> handler;

    public BaseHandler(Handler<T> handler) {
        this.handler = handler;
    }

//    public String handleHttpRequest(HttpExchange exchange) throws IOException {
//        log.info("Got a request to add resources");

    // TODO: refactor out common logic between ad@d/list handlers (will also be used for a future 'remove' handler)
//        verifyRequest(exchange);
//
//        final Map<String, List<String>> parameters = parseRequestParameters(exchange);
//        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);
//
//        if (null == paths) {
//            throw new InvalidRequest("Error: Missing '" + FILE_PATHS_PARAMETER + "' parameter");
//
//        }
//
//        return act(paths);
//    }

    public String doHandle(HttpExchange exchange) throws IOException {
        verifyRequest(exchange);
        T parameters = parseRequestParameters(exchange);
        return handler.act(parameters);
    }


//    private T parseRequestParameters(final HttpExchange exchange) {
//        handler.typeForJsonParsing();
//        return null;
//    }

    private T parseRequestParameters(HttpExchange exchange) throws IOException {
        final String requestBody = readRequestBody(exchange);
        final Optional<Type> type = handler.typeForJsonParsing();
        if (!type.isPresent()) {
            return null;
        }

        final T parameters;
        try {
            parameters = gson.fromJson(requestBody, type.get());
        } catch (JsonSyntaxException ex) {
            throw new InvalidRequest("Failed to parse JSON to type " + type);
        }
        return parameters;
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

    private void verifyRequest(HttpExchange exchange) {
        if (!isJsonContentType(exchange)) {
            throw new InvalidRequest("Expected request with JSON content");
        }
    }

    private boolean isJsonContentType(HttpExchange exchange) {
        return Optional
                .ofNullable(exchange.getRequestHeaders().getFirst(HttpConstants.CONTENT_TYPE))
                .filter(type -> type.startsWith(HttpConstants.APPLICATION_JSON))
                .isPresent();
    }
}
