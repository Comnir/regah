package com.jefferson.regah;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

class SharedResourcesTests {

    private SharedResources sharedResources;

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
        final File someFile = new File(this.getClass().getResource("/share/ForSharing.txt").getFile());
        sharedResources.share(someFile);

        Assertions.assertEquals(1, sharedResources.getResources().size(),
                "One file should be shared after adding a file");
    }

    @Test
    void sharedFileHasSamePath() {
        final SharedResources sharedResources = new SharedResources();
        final File someFile = new File(this.getClass().getResource("/share/ForSharing.txt").getFile());
        sharedResources.share(someFile);

        final File actualSharedFile = sharedResources.getResources().toArray(new File[0])[0];
        Assertions.assertEquals(someFile.getPath(), actualSharedFile.getPath(),
                "Shared file path doesn't match the added one");
    }

    @Test
    void nothingSharedAfterFileIsUnshared() {
        final SharedResources sharedResources = new SharedResources();
        final File someFile = new File(this.getClass().getResource("/share/ForSharing.txt").getFile());
        sharedResources.share(someFile);

        final File actualSharedFile = sharedResources.getResources().toArray(new File[0])[0];
        sharedResources.unshare(actualSharedFile);

        Assertions.assertEquals(0, sharedResources.getResources().size(),
                "File shouldn't be shared after it's unshared.");
    }

    @Test
    void sharingFoldersSharesAllResourcesInside() {
        final File folder = new File(this.getClass().getResource("/share").getFile());
        sharedResources.share(folder);

        final Set<File> expected = Set.of(
                new File(this.getClass().getResource("/share").getFile()),
                new File(this.getClass().getResource("/share/ForSharing.txt").getFile()),
                new File(this.getClass().getResource("/share/subFolder").getFile()),
                new File(this.getClass().getResource("/share/subFolder/subFile1.txt").getFile()),
                new File(this.getClass().getResource("/share/subFolder/subFile2.txt").getFile())
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
                "Shared folder doesn't match the actually shared files. Missing files: " + missingFiles
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
                String.format("A file should be considered 'shared' when it's parent is shared.")
        );
    }
}
