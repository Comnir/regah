package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.Handler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class ListResourcesHandler implements Handler<Object> {
    private static final Logger log = LogManager.getLogger(ListResourcesHandler.class);
    private static final Gson gson = new Gson();

    private final SharedResources sharedResources;

    public ListResourcesHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public Optional<Type> typeForJsonParsing() {
        return Optional.empty();
    }

    @Override
    public String act(Object parameters) {
        return gson.toJson(Map.of("results", sharedResources.getResources()));
    }
}
