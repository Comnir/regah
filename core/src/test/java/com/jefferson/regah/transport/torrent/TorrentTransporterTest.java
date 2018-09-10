package com.jefferson.regah.transport.torrent;

import com.jefferson.regah.transport.FailureToPrepareForDownload;
import com.jefferson.regah.transport.InvalidTransportData;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TorrentTransporterTest {
    private Path temporaryFolderSeeder;
    private Path temporaryFolderDownloader;
    private File sharedFile;
    private Torrent torrent;

    @BeforeEach
    void setup() throws IOException, NoSuchAlgorithmException, InterruptedException {
        temporaryFolderSeeder = Files.createTempDirectory("regah-test");
        temporaryFolderDownloader = Files.createTempDirectory("regah-test");
        sharedFile = File.createTempFile("sharedFile", "txt", null);

        final Path source = Paths.get(this.getClass()
                .getResource("/share/ForSharing.txt")
                .getPath());
        Files.copy(source, sharedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        sharedFile.deleteOnExit();
        torrent = Torrent.create(sharedFile, null, "regah");
    }

    @AfterEach
    void teardown() throws IOException {
        deleteFolder(temporaryFolderSeeder);
        deleteFolder(temporaryFolderDownloader);
    }

    private void deleteFolder(Path temporaryFolderSeeder) throws IOException {
        try (final Stream<Path> walk = Files.walk(temporaryFolderSeeder)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void downloadFileWithDownloaderAndSeeder() throws IOException, NoSuchAlgorithmException {
        final TorrentSeeder seeder = new TorrentSeeder();
        final Peer peer = seeder.seedSharedTorrent(300, new SharedTorrent(torrent, sharedFile.getParentFile()), InetAddress.getByName("0.0.0.0"));

        final SharedTorrent downloadTorrent = new SharedTorrent(torrent, temporaryFolderDownloader.toFile());
        final TorrentDownloader downloader = new TorrentDownloader();
        downloader.downloadSharedTorrent(downloadTorrent, peer, InetAddress.getByName("0.0.0.0"));

        assertDownloadedFileEqualToOrigin(sharedFile);
    }

    @Test
    void downloadFileWithDownloaderAndTransporterAsSeeder() throws IOException, FailureToPrepareForDownload, NoSuchAlgorithmException, InvalidTransportData {
        final TorrentTransportData transportData = (TorrentTransportData) new TorrentTransporter()
                .dataForDownloading(sharedFile);

        final SharedTorrent downloadTorrent = new SharedTorrent(torrent, temporaryFolderDownloader.toFile());
        final TorrentDownloader downloader = new TorrentDownloader();
        downloader.downloadSharedTorrent(downloadTorrent, transportData.getSeedingPeer(), InetAddress.getByName("0.0.0.0"));

        assertDownloadedFileEqualToOrigin(sharedFile);
    }

    @Test
    void downloadFileWithSeederAndTransporterAsDownloader() throws IOException, NoSuchAlgorithmException {
        final TorrentSeeder seeder = new TorrentSeeder();
        final Peer peer = seeder.seedSharedTorrent(60, new SharedTorrent(torrent, sharedFile.getParentFile()), InetAddress.getByName("0.0.0.0"));
        final TorrentTransportData transportData = new TorrentTransportData("ooo", peer, torrent.getEncoded());

        final TorrentTransporter downloader = new TorrentTransporter();
        downloader.downloadWithData(transportData, temporaryFolderDownloader);

        assertDownloadedFileEqualToOrigin(sharedFile);
    }

    @Test
    void seedAndDownloadWithTransporter() throws IOException, NoSuchAlgorithmException, FailureToPrepareForDownload {
        final TorrentTransportData transportData = (TorrentTransportData) new TorrentTransporter()
                .dataForDownloading(sharedFile);

        final TorrentTransporter downloader = new TorrentTransporter();
        downloader.downloadWithData(transportData, temporaryFolderDownloader);

        assertDownloadedFileEqualToOrigin(sharedFile);
    }

    private void assertDownloadedFileEqualToOrigin(File sharedFile) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] originalHash = md.digest(Files.readAllBytes(sharedFile.toPath()));
        final File targetFile = temporaryFolderDownloader.resolve(sharedFile.getName()).toFile();
        final byte[] resultHash = md.digest(Files.readAllBytes(targetFile.toPath()));
        assertArrayEquals(originalHash, resultHash, "Downloaded file's hash doesn't match the origin file's.");
    }
}