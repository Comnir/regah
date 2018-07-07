package com.jefferson.regah.transport;

public class UnsupportedTransportType extends RuntimeException {
    public UnsupportedTransportType(final String message) {
        super(message);
    }
}
