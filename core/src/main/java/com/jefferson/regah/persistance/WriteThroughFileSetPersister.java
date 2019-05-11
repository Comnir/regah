package com.jefferson.regah.persistance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WriteThroughFileSetPersister implements FileSetPersister {
    private static final Logger log = LogManager.getLogger(WriteThroughFileSetPersister.class);
    private final File outputFile;

    public WriteThroughFileSetPersister(File dataFolder, String name) throws IOException {
        outputFile = new File(dataFolder, name + ".files.set");

        if (!outputFile.exists() && !outputFile.createNewFile()) {
            final String message = "Failed to create file for persisting " + name;
            log.error(message);
            throw new IOException(message);
        }
    }

    @Override
    public void add(File t) throws IOException {
        addToPersisted(t.getAbsolutePath());
    }

    private void addToPersisted(String path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(outputFile, true)) {
            fileWriter.append(path);
            fileWriter.append(System.lineSeparator());
        }
    }

    @Override
    public void remove(File t) throws IOException {
        final String toRemove = t.getAbsolutePath();
        final Set<String> toWrite;
        try (Stream<String> fileReader = new BufferedReader(new FileReader(outputFile)).lines()) {
            toWrite = fileReader.filter(path -> !toRemove.equals(path))
                    .collect(Collectors.toSet());
        }

        if (!outputFile.delete() || !outputFile.createNewFile()) {
            throw new IOException("Failed to clear persisted set.");
        }

        for (final String path : toWrite) {
            addToPersisted(path);
        }
    }

    @Override
    public Set<File> getPersisted() throws IOException {
        try (Stream<String> fileReader = new BufferedReader(new FileReader(outputFile)).lines()) {
            return fileReader.map(File::new)
                    .collect(Collectors.toSet());
        }
    }
}
