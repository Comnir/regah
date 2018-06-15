package com.jefferson.regah.handler;

import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Responder {
    private static final Logger log = LogManager.getLogger(Responder.class);

    Responder() {
    }

    public void respondWithJson(HttpExchange exchange, String responseJson, int responseCode) {
        try {
            sendResponseThrowing(exchange, responseJson, responseCode);
        } catch (IOException e) {
            log.error("Error encountered while writing response. ", e);
        }
    }

    private void sendResponseThrowing(HttpExchange exchange, String responseJson, int responseCode) throws IOException {
        exchange.getResponseHeaders().add(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(responseCode, responseJson.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseJson.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }
}