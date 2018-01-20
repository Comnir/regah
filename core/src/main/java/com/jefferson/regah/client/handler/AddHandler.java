package com.jefferson.regah.client.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AddHandler implements HttpHandler {
    private static final Logger log = LogManager.getLogger(AddHandler.class);
    private static final Gson gson = new Gson();

    static final String FILE_PATHS_PARAMETER = "paths";

    private final SharedResources sharedResources;

    public AddHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("Got a request to add resources");

        if (!HttpConstants.APPLICATION_JSON.equals(exchange.getRequestHeaders().getFirst(HttpConstants.CONTENT_TYPE))) {
            final String responseJson = gson.toJson(Map.of(
                    HttpConstants.ERROR_REASON,
                    "Invalid request format!"));

            sendResponse(exchange, responseJson, 400);
            return;
        }

        final StringBuilder stringBuilder = new StringBuilder();
        try (final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            stringBuilder.append(bufferedReader.readLine());
        }
        final Map<String, List<String>> parameters = gson.fromJson(stringBuilder.toString(),
                TypeToken.getParameterized(Map.class, String.class, List.class).getType());
        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);

        sharedResources.share(new File(paths.get(0)));

        sendResponse(exchange, "", 200);
    }

    private void sendResponse(HttpExchange exchange, String responseJson, int responseCode) throws IOException {
        exchange.getResponseHeaders().add(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(responseCode, responseJson.length());
        final OutputStream os = exchange.getResponseBody();
        os.write(responseJson.getBytes(StandardCharsets.UTF_8));
        os.flush();
    }
}
