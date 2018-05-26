package com.jefferson.regah.handler;

import com.jefferson.regah.client.handler.InvalidRequest;
import com.jefferson.regah.client.handler.RequestProcessingFailed;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorWrappingHandler<T> implements HttpHandler {
    private static final Logger log = LogManager.getLogger(ErrorWrappingHandler.class);
    private final HttpHandler handler;

    private final Handler resultingHandler;

    private final Responder responder = new Responder();

    public ErrorWrappingHandler(HttpHandler handler) {
        this.handler = handler;
        this.resultingHandler = null;
    }

    public ErrorWrappingHandler(Handler resultingHandler) {
        this.handler = null;
        this.resultingHandler = resultingHandler;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (null == exchange) {
            throw new IllegalArgumentException("Got null as argument.");
        }
        try {
            if (null != handler) {
                handler.handle(exchange);
            }
            if (null != resultingHandler) {
                final String jsonResponse = resultingHandler.handleHttpRequest(exchange);
                responder.respondeWithJson(exchange, jsonResponse, 200);
            }
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
