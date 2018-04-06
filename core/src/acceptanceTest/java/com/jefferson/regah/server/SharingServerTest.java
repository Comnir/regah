package com.jefferson.regah.server;

import com.jefferson.regah.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class SharingServerTest {

    @BeforeEach
    void startApplication() {
        Application.main(null);
    }

    @Test
    void emptyWhenNothingShared() {

        given().
                param("", "").
                when().
                get("http://localhost:42424/listShared").
                then().
                assertThat().body("results", is(empty()));
    }
}