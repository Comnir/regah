package com.jefferson.regah;

import com.jefferson.regah.persistance.FileSetPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class SharedResources {
    private static final Logger log = LogManager.getLogger(SharedResources.class);
    private final Set<File> resources = new HashSet<>();
    private final FileSetPersister persister;

    @Inject
    public SharedResources(@Named("sharedFilesPersister") FileSetPersister persister) {
        this.persister = persister;
        try {
            resources.addAll(persister.getPersisted());
        } catch (IOException e) {
            log.error("Failed to load previously shared files.", e);
        }
    }

    public SharedResources() {
        this(FileSetPersister.NULL_PERSISTER);
    }

    public Set<File> getResources() {
        if (resources.stream().anyMatch(f -> !f.exists())) {
            log.warn("Some of the shared files don't exist and won't be listed.");
        }
        return resources.stream()
                .filter(File::exists)
                .collect(Collectors.toSet());
    }

    public void share(File file) {
        log.trace("File share requested: {}", file);

        if (resources.contains(file)) {
            return;
        }

        if (!file.isAbsolute()) {
            throw new IllegalArgumentException("Shared file should have absolute path.");
        }

        try {
            persister.add(file);
        } catch (IOException e) {
            log.error("Failed to persist information about new shared file. This means this file won't be shared after restarting the application. File: {}", file, e);
        }

        resources.add(file);
    }

    void unshare(File file) {
        try {
            persister.remove(file);
        } catch (IOException e) {
            log.error("Failed to persist information about a file being removed from shared files: {}", file);
        }

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
