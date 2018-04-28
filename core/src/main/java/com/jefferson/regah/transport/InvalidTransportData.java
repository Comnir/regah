package com.jefferson.regah.transport;

public class InvalidTransportData extends Exception {
    InvalidTransportData(String s) {
        this(s, null);
    }

    public InvalidTransportData(String message, Throwable cause) {
        super(message, cause);
    }
}
