package com.jefferson.regah;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SharedResources {
    final private Set<File> resources = new HashSet<>();

    public Set<File> getResources() {
        return resources.stream()
                 .map(File::toPath)
                 .flatMap(path -> {
                     if (!Files.isDirectory(path)) {
                         return Stream.of(path);
                     }
                     try {
                         return Files.walk(path);
                     } catch (IOException e) {
                         e.printStackTrace();
                         throw new RuntimeException("Failed to list files in directory: " + path, e);
                     }
                 })
                 .map(Path::toFile)
                .collect(Collectors.toSet());
    }

    public void share(File file) {
        resources.add(file);
    }

    public void unshare(File file) {
        resources.remove(file);
    }

    public boolean isShared(File file) {
        return resources.contains(file);
    }
}
