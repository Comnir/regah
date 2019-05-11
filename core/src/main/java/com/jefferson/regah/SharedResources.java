package com.jefferson.regah;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class SharedResources {
    private static final Logger log = LogManager.getLogger(SharedResources.class);
    private final Set<File> resources = new HashSet<>();

    public Set<File> getResources() {
        if (resources.stream().anyMatch(f -> !f.exists())) {
            log.warn("Some of the shared files don't exist and won't be listed.");
        }
        return resources.stream()
                .filter(File::exists)
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
        log.trace("File share requested: {}", file);

        if (!file.isAbsolute()) {
            throw new IllegalArgumentException("Shared file should have absolute path.");
        }

        resources.add(file);
    }

    void unshare(File file) {
        resources.remove(file);
    }

    public boolean isShared(final File file) {
        if (resources.contains(file)) {
            log.trace("Queried file exact path is shared: {}", file);
            return true;
        }

        // Possible optimization: for each sub-path of the given file, check whether it's shared.l
        final boolean ancestorShared = resources.stream()
                .anyMatch(sharedResource ->
                        file.toPath().startsWith(sharedResource.toPath()));

        log.trace("Queried file's ancestor is shared: {}. File path: {}", ancestorShared, file);
        return ancestorShared;
    }

    public void unshareAll() {
        resources.clear();
    }
}
