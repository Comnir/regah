package com.jefferson.regah.persistance;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WriteThroughFileSetPersisterTest {
    private File dataFolder;

    @BeforeEach
    void setup() throws IOException {
        Path tempFolder = Files.createTempDirectory("regah-test");
        dataFolder = tempFolder.resolve("data-folder").toFile();
        assertTrue(dataFolder.mkdir());
    }

    @AfterEach
    void teardown() throws IOException {
        FileUtils.deleteDirectory(dataFolder);
    }

    @Test
    void twoFilesInPersistedWhenTwoDifferentFilesWereAdded() throws IOException {
        final File file = new File("/file/to/add/1");
        final File otherFile = new File("/file/to/add/2");
        final FileSetPersister persister = new WriteThroughFileSetPersister(dataFolder, "test");
        persister.add(file);
        persister.add(otherFile);

        final Set<File> persisted = persister.getPersisted();
        assertEquals(2, persisted.size(),
                "All added files should be in persisted. Actual persisted: " + persisted);
        assertTrue(persisted.contains(file), "Expected file to be in persisted set." +
                " File: " + file + ", persisted: " + persisted);
        assertTrue(persisted.contains(otherFile), "Expected file to be in persisted set." +
                " File: " + otherFile + ", persisted: " + persisted);
    }

    @Test
    void addedFileNotInPersistedWhenPersistingObjectCreatedWithDifferentName() throws IOException {
        final File file = new File("/file/to/add/1");

        final String oneName = "aName";
        final String differentName = "aDifferentName";

        new WriteThroughFileSetPersister(dataFolder, oneName).add(file);

        final WriteThroughFileSetPersister persisterWithDifferentName = new WriteThroughFileSetPersister(dataFolder, differentName);

        final Set<File> persistedFromDifferentName = persisterWithDifferentName.getPersisted();
        assertTrue(persistedFromDifferentName.isEmpty(), "Persisted set expected to be empty. Persisted set: " + persistedFromDifferentName);
    }

    @Test
    void addedFileNotInPersistedSetWhenItWasRemoved() throws IOException {
        final File file = new File("/file/to/add/and/remove");
        final FileSetPersister persister = new WriteThroughFileSetPersister(dataFolder, "test");
        persister.add(file);

        persister.remove(file);

        final Set<File> persisted = persister.getPersisted();
        assertTrue(persisted.isEmpty(), "Expected persisted set to be empty, after file was removed." +
                " File: " + file + ", persisted: " + persisted);
    }

    @Test
    void fileIsInPersistedSetWhenCreatingPersistedSetWithSameFolder() throws IOException {
        final File file = new File("/file/to/add/and/check/in/new/object");
        new WriteThroughFileSetPersister(dataFolder, "test").add(file);

        final Set<File> persisted = new WriteThroughFileSetPersister(dataFolder, "test").getPersisted();
        assertTrue(persisted.contains(file), "Expected file to be in persisted set." +
                " File: " + file + ", persisted: " + persisted);
    }

    @Test
    void oneFileInPersistedWhenAddingTwoAndRemovingOne() throws IOException {
        final File file = new File("/file/to/add");
        final File fileToRemove = new File("/file/to/add/and/remove");
        final WriteThroughFileSetPersister persister = new WriteThroughFileSetPersister(dataFolder, "test");
        persister.add(file);
        persister.add(fileToRemove);

        persister.remove(fileToRemove);

        final Set<File> persisted = new WriteThroughFileSetPersister(dataFolder, "test").getPersisted();
        assertEquals(1, persisted.size(), "A single should be in persisted set.");
        assertTrue(persisted.contains(file), "Expected the file which was not deleted to be in persisted set." +
                " File: " + file + ", persisted: " + persisted);
    }
}