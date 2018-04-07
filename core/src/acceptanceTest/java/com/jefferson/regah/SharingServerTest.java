package com.jefferson.regah;

import com.google.gson.Gson;
import com.jefferson.regah.server.handler.HttpConstants;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class SharingServerTest {
    private static final int SERVER_PORT = 42424;
    private static final int MANAGEMENT_PORT = 42421; // TODO: make this port configureble in Application
    private static final Gson GSON = new Gson();
    private Application application;

    //    @Mock
    private SharedResources sharedResources;

    @BeforeEach
    void startApplication() {
//        MockitoAnnotations.initMocks(this);
        sharedResources = new SharedResources();
        application = new Application(SERVER_PORT, sharedResources);
        application.start();
    }

    @AfterEach
    void stopApplication() {
        application.stop();
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
}