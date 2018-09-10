package com.jefferson.regah.transport.torrent;

import com.jefferson.regah.transport.FailureToPrepareForDownload;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class FolderTransportTest {
    private Path tempForSeeder;
    private Path tempForDownloader;

    @BeforeEach
    public void setup() throws IOException {
        tempForSeeder = java.nio.file.Files.createTempDirectory("regah-test-seeder");
        tempForDownloader = java.nio.file.Files.createTempDirectory("regah-test-downloader");
        final Path sourceFolder = Paths.get(this.getClass()
                .getResource("/share")
                .getPath());

        FileUtils.copyDirectory(sourceFolder.toFile(), tempForSeeder.toFile());
    }

    @AfterEach
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(tempForSeeder.toFile());
        FileUtils.deleteDirectory(tempForDownloader.toFile());
    }

    @Test
    void allFilesMatchOriginWhenDownloadingFolderAsPath() throws IOException, NoSuchAlgorithmException, FailureToPrepareForDownload {
        final TorrentTransportData transportData = (TorrentTransportData) new TorrentTransporter()
                .dataForDownloading(tempForSeeder.toFile());

        final TorrentTransporter downloader = new TorrentTransporter();
        downloader.downloadWithData(transportData, tempForDownloader);
        try (final Stream<Path> files = Files.walk(tempForSeeder)) {
            files.map(p -> tempForSeeder.relativize(p))
                    .filter(p -> p.toFile().isFile())
                    .forEach(p -> assertFilesEqual(tempForSeeder.resolve(p).toFile(),
                            tempForDownloader.resolve(p).toFile())
                    );
        }
    }

    private void assertFilesEqual(final File f1, final File f2) {
        if (!f1.exists() || !f2.exists()) {
            final String f1Missing = f1.exists() ? "" : String.format("%s doesn't exist ", f1);
            final String f2Missing = f2.exists() ? "" : String.format("%s doesn't exist ", f2);
            fail(String.format("Assertion failure: %s%s", f1Missing, f2Missing));
        }

        try {
            if (com.google.common.io.Files.equal(f1, f2)) {
                return;
            }

            fail(String.format("Files are not equal: %s %s", f1, f2));
        } catch (IOException e) {
            fail(String.format("Failed to check equality of: %s %s with: %s", e, f1, f2));
        }
        fail(String.format("Files not equal: %s %s", f1, f2));
    }

    //    @Test // for debugging
    public void createSharedTorrent() throws InterruptedException, NoSuchAlgorithmException, IOException {
        final File file = tempForSeeder.toFile();
        final Torrent torrent;
        final SharedTorrent sharedTorrent;
        if (file.isFile()) {
            torrent = Torrent.create(file, null, "regah");
            sharedTorrent = new SharedTorrent(torrent, file.getParentFile(), true);
        } else {
            final List<File> listing = listFolderContentsRecursively(file);
            torrent = Torrent.create(file, listing, null, "regah");
            List<File> files = new ArrayList<File>(FileUtils.listFiles(file, TrueFileFilter.TRUE, TrueFileFilter.TRUE));
            sharedTorrent = new SharedTorrent(torrent, file.getParentFile(), true);
//            final FileStorage fileStorage = ((FileCollectionStorage) sharedTorrent.bucket).files.get(0);
//            assertTrue(fileStorage.current.exists());
//            assertEquals(fileStorage.current, fileStorage.target);
        }
        System.out.println(sharedTorrent);
    }

    private List<File> listFolderContentsRecursively(final File folder) throws IOException {
        try (final Stream<Path> folderContents = Files.walk(folder.toPath())) {
            return folderContents
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .collect(Collectors.toList());
        }
    }
}
