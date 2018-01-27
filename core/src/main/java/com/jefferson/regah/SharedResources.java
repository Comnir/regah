package com.jefferson.regah;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SharedResources {
    private static final Logger log = LogManager.getLogger(SharedResources.class);
    final private Set<File> resources = new HashSet<>();

    public Set<File> getResources() {
        if (resources.stream().anyMatch(f -> !f.exists())) {
            log.warn("Some of the shared files don't exist and won't be listed.");
        }
        return resources.stream()
                .filter(File::exists)
                .flatMap(SharedResources::streamOfFiles)
                .collect(Collectors.toSet());
    }

    private static Stream<? extends File> streamOfFiles(File file) {
        if (!file.isDirectory()) {
            return Stream.of(file);
        }
        try (final Stream<Path> ps = Files.walk(file.toPath())) {
            return Arrays.stream(ps.toArray(Path[]::new))
                    .map(Path::toFile);
        } catch (IOException e) {
            log.error("Failed to list files from " + file + " - " + e.getMessage());
            throw new RuntimeException("Failed to list files in directory: " + file, e);
        }
    }

    public void share(File file) {
        resources.add(file);
    }

    void unshare(File file) {
        resources.remove(file);
    }

    public boolean isShared(File file) {
        return resources.contains(file);
    }
}
