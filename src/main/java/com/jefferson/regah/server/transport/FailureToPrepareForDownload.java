package com.jefferson.regah.server.transport;

public class FailureToPrepareForDownload extends Exception {
    public FailureToPrepareForDownload(String message, Throwable cause) {
        super(message, cause);
    }
}
