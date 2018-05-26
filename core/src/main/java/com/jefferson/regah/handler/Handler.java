package com.jefferson.regah.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public interface Handler extends HttpHandler {
    default String handleHttpRequest(final HttpExchange exchange) throws IOException {
        return null;
    }

    @Override
    default void handle(final HttpExchange exchange) {
    }
}
