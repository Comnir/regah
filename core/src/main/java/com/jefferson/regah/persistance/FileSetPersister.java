package com.jefferson.regah.persistance;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public interface FileSetPersister {
    void add(File t) throws IOException;

    void remove(File t) throws IOException;

    Set<File> getPersisted() throws IOException;

    FileSetPersister NULL_PERSISTER = new FileSetPersister() {
        @Override
        public void add(File t) {

        }

        @Override
        public void remove(File t) {

        }

        @Override
        public Set<File> getPersisted() {
            return Collections.emptySet();
        }
    };
}
