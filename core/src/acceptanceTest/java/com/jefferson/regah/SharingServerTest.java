package com.jefferson.regah;

import com.google.gson.Gson;
import com.jefferson.regah.server.handler.HttpConstants;
import com.jefferson.regah.transport.InvalidTransportData;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.Transporter;
import com.jefferson.regah.transport.UnsupportedTransportType;
import com.jefferson.regah.transport.serialization.TransportDataDeserializer;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SharingServerTest {
    private static final int SERVER_PORT = 42424;
    private static final int MANAGEMENT_PORT = 42421; // TODO: make this port configurable in Application
    private static final Gson GSON = new Gson();
    private Application application;

    private SharedResources sharedResources;

    private Path temporaryFolder;

    @BeforeEach
    void startApplication() throws IOException {
        temporaryFolder = Files.createTempDirectory("regah-test");
        sharedResources = new SharedResources();
        application = new Application(SERVER_PORT, sharedResources, temporaryFolder.toFile());
        application.start();
    }

    @AfterEach
    void stopApplication() throws IOException {
        application.stop();
        try (final Stream<Path> folderTree = Files.walk(temporaryFolder)) {
            folderTree.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .map(File::delete)
                    .forEach(f -> System.out.println(String.format("Not deleted: %s", f)));
        }
    }

    @Test
    void emptyWhenNothingShared() {
        given().
                param("", "").
                when().
                get(String.format("http://localhost:%d/listShared", SERVER_PORT)).
                then().
                assertThat().body("results", is(empty()));
    }

    @Test
    void allSharedFilesAreInList() {
        final List<String> absolutePaths = Stream.of("/share/ForSharing.txt",
                "/share/subFolder/subFile1.txt",
                "/share/subFolder/subFile2.txt")
                .map(s -> this.getClass().getResource(s).getFile())
                .collect(Collectors.toList());

        final String requestBody = GSON.toJson(Map.of("paths", absolutePaths));
        given()
                .header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON)
                .body(requestBody)
                .when()
                .post(String.format("http://localhost:%d/add", MANAGEMENT_PORT));

        final Response response = given().
                param("", "").
                when().
                get(String.format("http://localhost:%d/listShared", SERVER_PORT));


        final ValidatableResponse validatableResponse = response.then().
                assertThat().body("results", hasSize(3));

        absolutePaths.forEach(path -> validatableResponse.and()
                .body("results", hasItem(Map.of("path", path))));
    }

    @Test
    void downloadSharedFile() throws IOException, NoSuchAlgorithmException, UnsupportedTransportType, InvalidTransportData {
        final List<String> absolutePaths = Stream.of("/share/ForSharing.txt",
                "/share/subFolder/subFile1.txt",
                "/share/subFolder/subFile2.txt")
                .map(s -> this.getClass().getResource(s).getFile())
                .collect(Collectors.toList());

        final String addRequestBody = GSON.toJson(Map.of("paths", absolutePaths));
        given()
                .header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON)
                .body(addRequestBody)
                .when()
                .post(String.format("http://localhost:%d/add", MANAGEMENT_PORT));

        final JsonPath sharedListJson = given().
                param("", "").
                when().
                get(String.format("http://localhost:%d/listShared", SERVER_PORT)).jsonPath();

        String path = sharedListJson.getString("results[1].path");

        final Response response = given()
                .header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON)
                .body(new Gson().toJson(Map.of("filePath", path)))
                .when()
                .get(String.format("http://localhost:%d/fetchResources", SERVER_PORT));

        final TransportDataDeserializer deserializer = new TransportDataDeserializer(response.body().asString());
        final TransportData data = deserializer.getTransportData();

        final File sharedFile = Paths.get(path).toFile();
        final Path downloadDestination = Files.createTempDirectory("regah-test-download");

        final Transporter transporter = deserializer.getTransporter();//new TorrentTransporter();
        transporter.downloadWithData(data, downloadDestination);

        MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] originalHash = md.digest(Files.readAllBytes(sharedFile.toPath()));
        final byte[] resultHash = md.digest(Files.readAllBytes(downloadDestination.resolve(sharedFile.getName())));
        assertArrayEquals(originalHash, resultHash);
    }
}