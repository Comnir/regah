package com.jefferson.regah.transport;

public interface TransportData {
    String asJson();

    String getId();

    void acceptVisitor(TransportDataVisitor visitor);
}
