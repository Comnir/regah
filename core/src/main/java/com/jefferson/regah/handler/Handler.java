package com.jefferson.regah.handler;

import java.lang.reflect.Type;
import java.util.Optional;

public interface Handler<T> {
    String act(T parameters);

    Optional<Type> typeForJsonParsing();
}
