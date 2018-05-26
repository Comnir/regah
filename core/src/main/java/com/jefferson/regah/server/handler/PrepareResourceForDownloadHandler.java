package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.client.handler.InvalidRequest;
import com.jefferson.regah.client.handler.RequestProcessingFailed;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.handler.Handler;
import com.jefferson.regah.transport.FailureToPrepareForDownload;
import com.jefferson.regah.transport.Transporter;
import com.jefferson.regah.transport.serialization.TransportDataSerializer;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class PrepareResourceForDownloadHandler implements Handler {
    private static final Logger log = LogManager.getLogger(PrepareResourceForDownloadHandler.class);
    private final static Gson gson = new Gson();

    static final String FILE_PATH_PARAMETER = "filePath";

    private final SharedResources sharedResources;
    private final Transporter transporter;
    private final ImmutableWrapper<Supplier<TransportDataSerializer>> transportDataConverterCreator;

    public PrepareResourceForDownloadHandler(SharedResources sharedResources, Transporter transporter) {
        this.sharedResources = sharedResources;
        this.transporter = transporter;
        transportDataConverterCreator = new ImmutableWrapper<>();
    }

    @Override
    public String handleHttpRequest(HttpExchange exchange) throws IOException {
        log.info("Fetch resources request");
        log.debug("Headers: " + exchange.getRequestHeaders());

        verifyRequest(exchange);

        final Map<String, String> parameters = parseRequestParameters(exchange);
        final File file = new File(parameters.get(FILE_PATH_PARAMETER));

        if (!sharedResources.isShared(file)) {
            throw new InvalidRequest("Requested file is not shared.");
        }

        try {
            return createTransportDataConverter().toJson(transporter.dataForDownloading(file));
        } catch (FailureToPrepareForDownload e) {
            throw new RequestProcessingFailed("Failed to prepare requested file for download. " + e.getMessage());
        }
    }

    private Map<String, String> parseRequestParameters(HttpExchange exchange) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        try (final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            stringBuilder.append(bufferedReader.readLine());
        }
        return gson.fromJson(stringBuilder.toString(),
                TypeToken.getParameterized(Map.class, String.class, String.class).getType());
    }

    private void verifyRequest(HttpExchange exchange) {
        if (!isJsonContentType(exchange)) {
            throw new InvalidRequest("Invalid request format!");
        }
    }

    private TransportDataSerializer createTransportDataConverter() {
        return transportDataConverterCreator.asOptional()
                .map(Supplier::get)
                .orElseGet(TransportDataSerializer::new);
    }

    private boolean isJsonContentType(HttpExchange exchange) {
        return Optional
                .ofNullable(exchange.getRequestHeaders().getFirst(HttpConstants.CONTENT_TYPE))
                .filter(type -> type.startsWith(HttpConstants.APPLICATION_JSON))
                .isPresent();
    }

    PrepareResourceForDownloadHandler setTransportDataConverterCreator(final Supplier<TransportDataSerializer> creator) {
        transportDataConverterCreator.set(creator);
        return this;
    }
}
