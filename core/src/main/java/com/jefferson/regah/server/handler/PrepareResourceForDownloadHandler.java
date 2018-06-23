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

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class PrepareResourceForDownloadHandler implements Handler<Map<String, String>> {
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
    public String act(final Map<String, String> parameters) {
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

    @Override
    public Optional<Type> typeForJsonParsing() {
        return Optional.of(TypeToken.getParameterized(Map.class, String.class, String.class).getType());
    }

    private TransportDataSerializer createTransportDataConverter() {
        return transportDataConverterCreator.asOptional()
                .map(Supplier::get)
                .orElseGet(TransportDataSerializer::new);
    }

    PrepareResourceForDownloadHandler setTransportDataConverterCreator(final Supplier<TransportDataSerializer> creator) {
        transportDataConverterCreator.set(creator);
        return this;
    }

}
