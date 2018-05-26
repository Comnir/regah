package com.jefferson.regah.client.handler;

public class RequestProcessingFailed extends RuntimeException {
    public RequestProcessingFailed(String message) {
        super(message);
    }
}
