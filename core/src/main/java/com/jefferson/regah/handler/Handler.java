package com.jefferson.regah.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

public interface Handler extends HttpHandler {
    default String handleHttpRequest(final HttpExchange exchange) throws IOException {
        return null;
    }

    Optional<Type> typeForJsonParsing();

    @Override
    default void handle(final HttpExchange exchange) {
    }
}
