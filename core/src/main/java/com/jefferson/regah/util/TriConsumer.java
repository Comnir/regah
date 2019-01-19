package com.jefferson.regah.util;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<T, U, R> {
    void accept(T t, U u, R r);

    default TriConsumer<T, U, R> andThen(TriConsumer<T, U, R> after) {
        Objects.requireNonNull(after);
        return after;
    }
}
