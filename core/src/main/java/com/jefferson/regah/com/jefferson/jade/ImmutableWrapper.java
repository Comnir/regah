package com.jefferson.regah.com.jefferson.jade;

import java.util.Optional;

public class ImmutableWrapper<T> {
    private T object;

    public ImmutableWrapper() {
    }

    public ImmutableWrapper(final T object) {
        this.object = object;
    }

    /**
     * @param object to initialize this wrapper.
     */
    public T set(final T object) {
        if (null != this.object) {
            throw new IllegalModification("Assigning to an already assigned object is not allowed.");
        }

        this.object = object;
        return object;
    }

    public T get() {
        return object;
    }

    public Optional<T> asOptional() {
        return Optional.ofNullable(object);
    }

    public boolean isPresent() {
        return object != null;
    }

    public boolean isEmpty() {
        return !isPresent();
    }

    @Override
    public String toString() {
        return isEmpty() ? "null" : object.toString();
    }
}
