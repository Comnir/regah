package com.jefferson.regah;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public class SharedResourcesTests {
    @Test
    public void noSharedResource() {
        final SharedResources sharedResources = new SharedResources();

        final Set<File> resources = sharedResources.getResources();
        Assertions.assertNotNull(resources, "Resources colection should not be null");
        Assertions.assertEquals(0, resources.size(),
                "There should be no shared resources in the initial state");
    }

    @Test
    public void oneSharedFile() {
        final SharedResources sharedResources = new SharedResources();
        final File someFile = new File(this.getClass().getResource("/sharedFoldersBasicSharingTest/ForSharing.txt").getFile());
        sharedResources.share(someFile);

        Assertions.assertEquals(1, sharedResources.getResources().size(),
                "One file should be shared after adding a file");
    }

    @Test
    public void sharedFileHasSamePath() {
        final SharedResources sharedResources = new SharedResources();
        final File someFile = new File(this.getClass().getResource("/sharedFoldersBasicSharingTest/ForSharing.txt").getFile());
        sharedResources.share(someFile);

        final File actualSharedFile = sharedResources.getResources().toArray(new File[0])[0];
        Assertions.assertEquals(someFile.getPath(), actualSharedFile.getPath(),
                "Shared file path doesn't match the added one");
    }

    @Test
    public void nothingSharedAfterFileIsUnshared() {
        final SharedResources sharedResources = new SharedResources();
        final File someFile = new File(this.getClass().getResource("/sharedFoldersBasicSharingTest/ForSharing.txt").getFile());
        sharedResources.share(someFile);

        final File actualSharedFile = sharedResources.getResources().toArray(new File[0])[0];
        sharedResources.unshare(actualSharedFile);

        Assertions.assertEquals(0, sharedResources.getResources().size(),
                "File shouldn't be shared after it's unshared.");
    }

    @Test
    public void sharingFoldersSharesAllResourcesInside() {
        final SharedResources sharedResources = new SharedResources();
        final File folder = new File(this.getClass().getResource("/sharedFoldersBasicSharingTest").getFile());
        sharedResources.share(folder);

        final Set<File> expected = Set.of(
                new File(this.getClass().getResource("/sharedFoldersBasicSharingTest").getFile()),
                new File(this.getClass().getResource("/sharedFoldersBasicSharingTest/ForSharing.txt").getFile()),
                new File(this.getClass().getResource("/sharedFoldersBasicSharingTest/subFolder").getFile()),
                new File(this.getClass().getResource("/sharedFoldersBasicSharingTest/subFolder/subFile1.txt").getFile()),
                new File(this.getClass().getResource("/sharedFoldersBasicSharingTest/subFolder/subFile2.txt").getFile())
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
}
