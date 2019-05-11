package com.jefferson.regah.persistance;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface FileSetPersister {
    void add(File t) throws IOException;

    void remove(File t) throws IOException;

    Set<File> getPersisted() throws IOException;
}
