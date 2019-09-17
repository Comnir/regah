package com.jefferson.regah;

import com.google.common.collect.ImmutableSet;
import com.jefferson.regah.persistance.FileSetPersister;
import com.jefferson.regah.persistance.WriteThroughFileSetPersister;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SharedResourcesTests {

    private SharedResources sharedResources;
    private static File someFile;

    @BeforeAll
    static void setupAll() {
        someFile = new File(SharedResourcesTests.class.getResource("/share/ForSharing.txt").getFile());
    }

    @BeforeEach
    void setUp() {
        sharedResources = new SharedResources();
    }

    @Test
    void noSharedResource() {
        final SharedResources sharedResources = new SharedResources();

        final Set<File> resources = sharedResources.getResources();
        Assertions.assertNotNull(resources, "Resources colection should not be null");
        Assertions.assertEquals(0, resources.size(),
                "There should be no shared resources in the initial state");
    }

    @Test
    void oneSharedFile() {
        final SharedResources sharedResources = new SharedResources();
        sharedResources.share(someFile);

        Assertions.assertEquals(1, sharedResources.getResources().size(),
                "One file should be shared after adding a file");
    }

    @Test
    void sharedFileHasSamePath() {
        final SharedResources sharedResources = new SharedResources();
        sharedResources.share(someFile);

        final File actualSharedFile = sharedResources.getResources().toArray(new File[0])[0];
        Assertions.assertEquals(someFile.getPath(), actualSharedFile.getPath(),
                "Shared file path doesn't match the added one");
    }

    @Test
    void nothingSharedAfterFileIsUnshared() {
        final SharedResources sharedResources = new SharedResources();
        sharedResources.share(someFile);

        final File actualSharedFile = sharedResources.getResources().toArray(new File[0])[0];
        sharedResources.unshare(actualSharedFile);

        Assertions.assertEquals(0, sharedResources.getResources().size(),
                "File shouldn't be shared after it's unshared.");
    }

    @Test
    void folderIsTheOnlyListedSharedResourceWhenSharingAfolder() {
        final File folder = new File(this.getClass().getResource("/share").getFile());
        sharedResources.share(folder);

        final Set<File> expected = Set.of(
                new File(this.getClass().getResource("/share").getFile())
        );

        final Set<File> actuallyShared = sharedResources.getResources();
        final String missingFiles = expected.stream()
                .filter(actual -> !actuallyShared.contains(actual))
                .map(File::getPath)
                .collect(Collectors.joining(","));
        final String extraFiles = actuallyShared.stream()
                .filter(actual -> !expected.contains(actual))
                .map(File::getPath)
                .collect(Collectors.joining(","));

        Assertions.assertEquals(expected.size(), actuallyShared.size(),
                "When sharing a folder, only the share folder should be returned as shared. Missing files: " + missingFiles
                        + System.lineSeparator() + "Extra files: " + extraFiles);
    }

    @Test
    void fileNotShared() {
        Assertions.assertFalse(
                sharedResources.isShared(new File(this.getClass().getResource("/noshare/NotForShare.txt").getFile())),
                "The file is unexpectedly shared"
        );
    }

    @Test
    void fileIsSharedWhenExactFileWasShared() {
        final File file = new File(this.getClass().getResource("/share/ForSharing.txt").getFile());
        sharedResources.share(file);

        Assertions.assertTrue(
                sharedResources.isShared(file),
                "The file should be shared"
        );
    }

    @Test
    void fileIsSharedWhenParentFolderWasShared() {
        final File childFile = new File(this.getClass().getResource("/share/ForSharing.txt").getFile());
        final File parentFolder = childFile.getParentFile();
        sharedResources.share(parentFolder);

        Assertions.assertTrue(
                sharedResources.isShared(childFile),
                "A file should be considered 'shared' when it's parent is shared."
        );
    }

    @Test
    void throwsWhenNonAbsolutePathIsShared() {
        final File file = new File("relativePath.txt");

        try {
            sharedResources.share(file);
            fail("Should throw an exception when relative path is shared");
        } catch (IllegalArgumentException e) {
            assertThat("Exception message should address absolute vs. relative path", e.getMessage(), StringContains.containsString("absolute"));
        } catch (Exception e) {
            fail("Should throw an IllegalArgumentException");
        }

    }

    @Test
    void persisterCalled_when_newFileIsShared(@TempDir File tempFolder) throws IOException {
        final FileSetPersister persister = mock(WriteThroughFileSetPersister.class);
        new SharedResources(persister).share(someFile);

        verify(persister).add(someFile);
    }

    @Test
    void persisterNotCalled_when_sameFileIsSharedTwice(@TempDir File tempFolder) throws IOException {
        final FileSetPersister persister = mock(WriteThroughFileSetPersister.class);
        final SharedResources sharedResources = new SharedResources(persister);
        sharedResources.share(someFile);

        final File anotherReferenceOfSomeFile = new File(someFile.getParent(), someFile.getName());
        assertEquals(someFile.getAbsolutePath(), anotherReferenceOfSomeFile.getAbsolutePath());
        sharedResources.share(anotherReferenceOfSomeFile);

        verify(persister).add(anotherReferenceOfSomeFile);
    }

    @Test
    void persisterCalled_when_unsharingFile(@TempDir File tempFolder) throws IOException {
        final FileSetPersister persister = mock(WriteThroughFileSetPersister.class);
        final SharedResources sharedResources = new SharedResources(persister);
        sharedResources.share(someFile);

        sharedResources.unshare(someFile);

        verify(persister).remove(someFile);
    }

    @Test
    void fileIsShared_when_fileIsInPersistedSharedFiles() throws IOException {
        final FileSetPersister persister = mock(WriteThroughFileSetPersister.class);
        when(persister.getPersisted()).thenReturn(ImmutableSet.of(someFile));
        final SharedResources sharedResources = new SharedResources(persister);

        assertTrue(sharedResources.isShared(someFile), "File should be shared");
    }
}
