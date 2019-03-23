package com.jefferson.regah.transport;

import com.google.inject.Inject;
import com.jefferson.regah.transport.serialization.TransportDataDeserializer;
import com.jefferson.regah.transport.serialization.TransportDataDeserializerFactory;

import java.nio.file.Path;

public class Downloader {
    private final TransportDataDeserializerFactory deserializerFactory;

    @Inject
    public Downloader(TransportDataDeserializerFactory deserializerFactory) {
        this.deserializerFactory = deserializerFactory;
    }

    public void download(String downloadData, Path destination) {
        final TransportDataDeserializer deserializer = createTransportDataDeserializer(downloadData);
        final TransportData transportData = deserializer.getTransportData();
        deserializer.getTransporter().downloadWithData(transportData, destination);
    }

    private TransportDataDeserializer createTransportDataDeserializer(String downloadData) {
        return deserializerFactory.create(downloadData);
    }
}
