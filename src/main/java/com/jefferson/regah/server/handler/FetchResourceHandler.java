package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.transport.FailureToPrepareForDownload;
import com.jefferson.regah.transport.Transporter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FetchResourceHandler implements HttpHandler {
    private static final Logger log = LogManager.getLogger(FetchResourceHandler.class);
    private final static Gson gson = new Gson();

    static final String FILE_PATH_PARAMETER = "filePath";

    private final SharedResources sharedResources;
    private final Transporter transporter;

    public FetchResourceHandler(SharedResources sharedResources, Transporter transporter) {
        this.sharedResources = sharedResources;
        this.transporter = transporter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("Fetch resources request");
        log.debug("Headers: " + exchange.getRequestHeaders());

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
        final Map<String, String> parameters = gson.fromJson(stringBuilder.toString(),
                TypeToken.getParameterized(Map.class, String.class, String.class).getType());
        final File file = new File(parameters.get(FILE_PATH_PARAMETER));

        if (!sharedResources.isShared(file)) {
            final String responseJson = gson.toJson(Map.of(
                    HttpConstants.ERROR_REASON,
                    "Requested file is not shared!"));

            sendResponse(exchange, responseJson, 400);
            return;
        }

        try {
            final String responseJson = transporter.getDownloadInfoFor(file).asJson();
            sendResponse(exchange, responseJson, 200);
        } catch (FailureToPrepareForDownload e) {
            final String responseJson = gson.toJson(Map.of(
                    HttpConstants.ERROR_REASON,
                    "Failed to prepare requested file for download. " + e.getMessage()));

            sendResponse(exchange, responseJson, 503);
        }
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
