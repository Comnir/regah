package com.jefferson.regah.client.handler;

import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.handler.Handler;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.serialization.TransportDataDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DownloadHandler implements Handler<Map<String, String>> {
    private static final Logger log = LogManager.getLogger(DownloadHandler.class);

    public static final String KEY_PATH = "path";
    public static final String KEY_DATA = "downloadData";
    private final Function<String, TransportDataDeserializer> deserializerFactory;

    public DownloadHandler() {
        this(TransportDataDeserializer::new);
    }

    public DownloadHandler(final Function<String, TransportDataDeserializer> deserializerFactory) {
        this.deserializerFactory = deserializerFactory;
    }

    @Override
    public String act(Map<String, String> parameters) {
        final Path destination = getPath(parameters);
        final String downloadData = getTransportDataString(parameters);

        final TransportDataDeserializer deserializer = createTransportDataDeserializer(downloadData);
        final TransportData transportData = deserializer.getTransportData();
        deserializer.getTransporter().downloadWithData(transportData, destination);

        return "";
    }

    private String getTransportDataString(Map<String, String> parameters) {
        return Optional.ofNullable(parameters.get(KEY_DATA))
                .orElseThrow(() -> new InvalidRequest("Missing destination folder parameter. Expected key: " + KEY_DATA));
    }

    private Path getPath(Map<String, String> parameters) {
        final String pathString = parameters.get(KEY_PATH);
        if (null == pathString) {
            throw new InvalidRequest("Missing transport-data string. Expected key: " + KEY_PATH);
        }
        final Path path = Paths.get(pathString);

        if (!path.toFile().exists()) {
            throw new InvalidRequest("Given destination path doesn't exist! Got: " + pathString);
        }

        return path;
    }

    private TransportDataDeserializer createTransportDataDeserializer(String downloadData) {
        return deserializerFactory.apply(downloadData);
    }

    @Override
    public Optional<Type> typeForJsonParsing() {
        return Optional.of(TypeToken.getParameterized(Map.class, String.class, String.class).getRawType());
    }
}
