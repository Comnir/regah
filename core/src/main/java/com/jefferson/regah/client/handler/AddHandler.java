package com.jefferson.regah.client.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.Handler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddHandler implements Handler<Map<String, List<String>>> {
    private static final Logger log = LogManager.getLogger(AddHandler.class);
    private static final Gson gson = new Gson();

    private static final String FILE_PATHS_PARAMETER = "paths";

    private final SharedResources sharedResources;

    public AddHandler(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    @Override
    public Optional<Type> typeForJsonParsing() {
        return Optional.of(TypeToken.getParameterized(Map.class, String.class, List.class).getType());
    }

    @Override
    public String act(final Map<String, List<String>> parameters) {
        final List<String> paths = parameters.get(FILE_PATHS_PARAMETER);

        if (null == paths) {
            throw new InvalidRequest("Error: Missing '" + FILE_PATHS_PARAMETER + "' parameter");
        }

        if (paths.isEmpty()) {
            log.warn("Add request got no paths to add.");
        } else {
            log.trace("Add request - got paths to add: " + paths);
            paths.stream()
                    .map(File::new)
                    .forEach(sharedResources::share);
        }

        return "";
    }

}
