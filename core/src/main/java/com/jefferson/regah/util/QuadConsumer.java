package com.jefferson.regah.util;

public interface QuadConsumer<T, U, R, S> {
    void accept(T t, U u, R r, S s);
}
