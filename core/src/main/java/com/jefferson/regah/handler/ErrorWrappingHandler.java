package com.jefferson.regah.handler;

import com.jefferson.regah.client.handler.InvalidRequest;
import com.jefferson.regah.client.handler.RequestProcessingFailed;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorWrappingHandler implements HttpHandler {
    private static final Logger log = LogManager.getLogger(ErrorWrappingHandler.class);
    private final HttpHandler handler;
    private final Responder responder = new Responder();

    public ErrorWrappingHandler(HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (null == exchange) {
            throw new IllegalArgumentException("Got null as argument.");
        }
        try {
            handler.handle(exchange);
        } catch (InvalidRequest e) {
            log.error("Got an invalid request. ", e);
            responder.respondeWithJson(exchange, e.getMessage(), 400);
        } catch (RequestProcessingFailed e) {
            log.error("Request handling failed", e);
            responder.respondeWithJson(exchange, e.getMessage(), 503);
        } catch (Exception e) {
            log.error("Request handling failed", e);
            responder.respondeWithJson(exchange,
                    "Error encountered while processing the request. " + e.getMessage(),
                    400);
        }
    }
}
