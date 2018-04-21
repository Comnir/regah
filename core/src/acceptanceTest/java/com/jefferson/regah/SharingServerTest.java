package com.jefferson.regah;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jefferson.regah.server.handler.HttpConstants;
import com.jefferson.regah.transport.TorrentTransportData;
import com.jefferson.regah.transport.TorrentTransporter;
import com.jefferson.regah.transport.torrent.ByteArrayTypeAdapter;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
        Files.walk(temporaryFolder)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
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
    void downloadSharedFile() throws IOException {
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
// response JSON example: {"id":"2bd4a48c-6416-48a4-a1e5-9b02ce8d9eae","seedingPeereerDto":{"ip":[127,0,0,1],"port":49152}}
        final TorrentTransportData data = new GsonBuilder()
                .registerTypeAdapter(byte[].class, new ByteArrayTypeAdapter())
                .create()
                .fromJson(response.body().asString(), TorrentTransportData.class);
        final Path downloadDestination = Files.createTempDirectory("regah-test-download");

        final TorrentTransporter transporter = new TorrentTransporter(downloadDestination.toFile());
        transporter.downloadWithData(data);

        Files.walk(downloadDestination).forEach(System.out::println);
    }
}