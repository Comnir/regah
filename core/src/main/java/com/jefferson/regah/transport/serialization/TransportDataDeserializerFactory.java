package com.jefferson.regah.transport.serialization;

public interface TransportDataDeserializerFactory {
    TransportDataDeserializer create(final String transportDataJson);
}
