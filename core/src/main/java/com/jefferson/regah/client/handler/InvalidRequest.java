package com.jefferson.regah.client.handler;

import com.google.gson.JsonSyntaxException;

public class InvalidRequest extends RuntimeException {
    private static final String GENERIC_ERROR = "Invalid request!";

    public InvalidRequest() {
        super(GENERIC_ERROR);
    }

    public InvalidRequest(String additionalMessage) {
        super(GENERIC_ERROR + additionalMessage);
    }

    public InvalidRequest(String error, JsonSyntaxException e) {
        super(GENERIC_ERROR + error, e);
    }
}
