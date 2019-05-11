package com.jefferson.regah;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jefferson.regah.client.handler.DownloadHandler;
import com.jefferson.regah.guice.ConfigurationModule;
import com.jefferson.regah.guice.HttpHandlersModule;
import com.jefferson.regah.server.handler.HttpConstants;
import com.jefferson.regah.transport.InvalidTransportData;
import com.jefferson.regah.transport.UnsupportedTransportType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SharingServerTest {
    @Inject
    @Named("sharing-server-port")
    private int SERVER_PORT;
    @Inject
    @Named("sharing-management-port")
    private int MANAGEMENT_PORT;
    private static final Gson GSON = new Gson();

    private static Application application;
    @Inject
    private SharedResources sharedResources;
    private static Path temporaryFolder;
    private static Injector injector;

    @BeforeAll
    static void startApplication() throws IOException {
        temporaryFolder = Files.createTempDirectory("regah-test");

        injector = Guice.createInjector(Arrays.asList(new ConfigurationModule(), new HttpHandlersModule()));
        application = injector.getInstance(Application.class);

        Runtime.getRuntime().addShutdownHook(new Thread(application::stop));
        application.start();
    }

    @BeforeEach
    void setup() {
        injector.injectMembers(this);
    }

    @AfterEach
    void cleanup() throws IOException {
        sharedResources.unshareAll();
        try (final Stream<Path> folderTree = Files.walk(temporaryFolder)) {
            folderTree.sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(temporaryFolder))
                    .map(Path::toFile)
                    .map(File::delete)
                    .forEach(f -> System.out.println(String.format("Not deleted: %s", f)));
        }
    }

    @AfterAll
    static void stopApplication() {
        application.stop();
    }

    @Test
    void emptyWhenNothingShared() {
        given().
                header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON).
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

        final Response response =
                given().
                        header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON).
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

        final JsonPath sharedListJson = given()
                .header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON).
                param("", "").
                when().
                get(String.format("http://localhost:%d/listShared", SERVER_PORT)).jsonPath();

        String sourcePath = sharedListJson.getString("results[1].path");

        final Map<String, String> mapForPrepare = Map.of("filePath", sourcePath);
        final Response responseOnPrepare = given()
                .header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON)
                .body(toJson(mapForPrepare))
                .when()
                .get(String.format("http://localhost:%d/prepareResourceForDownload", SERVER_PORT));

        final Path downloadDestination = Files.createTempDirectory("regah-test-download");
        final Map<String, String> map = Map.of(DownloadHandler.KEY_PATH, downloadDestination.toAbsolutePath().toString(),
                DownloadHandler.KEY_DATA, responseOnPrepare.body().asString());

        given().header(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON)
                .body(toJson(map))
                .when()
                .get(String.format("http://localhost:%d/download", MANAGEMENT_PORT));


        final MessageDigest md = MessageDigest.getInstance("MD5");
        final File sharedFile = Paths.get(sourcePath).toFile();
        final byte[] originalHash = md.digest(Files.readAllBytes(sharedFile.toPath()));
        final byte[] resultHash = md.digest(Files.readAllBytes(downloadDestination.resolve(sharedFile.getName())));
        assertArrayEquals(originalHash, resultHash, "Hash of original file and downloaded file should be equal.");
    }

    private String toJson(Map<String, String> mapForPrepare) {
        return GSON.toJson(mapForPrepare);
    }
}