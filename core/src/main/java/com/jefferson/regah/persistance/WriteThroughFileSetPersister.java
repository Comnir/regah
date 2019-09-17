package com.jefferson.regah.persistance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;

public class WriteThroughFileSetPersister implements FileSetPersister {
    private static final Logger log = LogManager.getLogger(WriteThroughFileSetPersister.class);
    private final File outputFile;

    public WriteThroughFileSetPersister(File dataFolder, String name) throws IOException {
        outputFile = new File(dataFolder, name + ".files.set");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            final String message = "Failed to create application data folder " + dataFolder;
            log.error(message);
            throw new IOException(message);
        }

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
        try (Writer writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8, APPEND)) {
            writer.append(path);
            writer.append(System.lineSeparator());
        }
    }

    @Override
    public void remove(File t) throws IOException {
        final String toRemove = t.getAbsolutePath();
        final Set<String> toWrite;
        try (Stream<String> lines = Files.newBufferedReader(outputFile.toPath(), StandardCharsets.UTF_8).lines()) {
            toWrite = lines.filter(path -> !toRemove.equals(path))
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
        try (Stream<String> lines = Files.newBufferedReader(outputFile.toPath(), StandardCharsets.UTF_8).lines()) {
            return lines.map(File::new)
                    .collect(Collectors.toSet());
        }
    }
}
