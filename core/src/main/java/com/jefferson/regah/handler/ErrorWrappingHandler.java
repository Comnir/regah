package com.jefferson.regah.handler;

import com.jefferson.regah.client.BaseHandler;
import com.jefferson.regah.client.handler.InvalidRequest;
import com.jefferson.regah.client.handler.RequestProcessingFailed;
import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public class ErrorWrappingHandler<T> implements HttpHandler {
    private static final Logger log = LogManager.getLogger(ErrorWrappingHandler.class);
    private final Handler resultingHandler;
    private final Responder responder;

    public ErrorWrappingHandler(Handler resultingHandler) {
        this(resultingHandler, new Responder());
    }

    public ErrorWrappingHandler(final Handler resultingHandler, final Responder responder) {
        this.resultingHandler = Objects.requireNonNull(resultingHandler);
        this.responder = responder;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (null == exchange) {
            throw new IllegalArgumentException("Got null as argument.");
        }
        try {
            if (handleOptions(exchange)) {
                return;
            }
            final String jsonResponse = new BaseHandler<T>(resultingHandler).doHandle(exchange);
            responder.respondWithJson(exchange, jsonResponse, 200);
        } catch (InvalidRequest e) {
            log.error("Got an invalid request. ", e);
            responder.respondWithJson(exchange, e.getMessage(), 400);
        } catch (RequestProcessingFailed e) {
            log.error("Request handling failed", e);
            responder.respondWithJson(exchange, e.getMessage(), 503);
        } catch (Exception e) {
            log.error("Request handling failed", e);
            responder.respondWithJson(exchange,
                    "Error encountered while processing the request. " + e.getMessage(),
                    400);
        }
    }

    private boolean handleOptions(HttpExchange exchange) throws IOException {
        if (!"OPTIONS".equals(exchange.getRequestMethod())) {
            return false;
        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", HttpConstants.CONTENT_TYPE);
        // responding to OPTIONS with content length of 0 is incorrect(?), since the browser (Chrome/Firefox)
        // doesn't start a new request for the actual GET/POST. Changing reponse length to -1 fixed this.
        exchange.sendResponseHeaders(200, -1);
        return true;
    }

    public static Builder builder(Handler resultingHandler) {
        return new Builder(resultingHandler);
    }

    public static class Builder {
        private final Handler resultingHandler;

        private Responder responder = new Responder();

        public Builder(Handler resultingHandler) {
            this.resultingHandler = resultingHandler;
        }

        public ErrorWrappingHandler build() {
            return new ErrorWrappingHandler(resultingHandler, responder);
        }

        public Builder setResponder(Responder responder) {
            this.responder = responder;
            return this;
        }
    }
}
