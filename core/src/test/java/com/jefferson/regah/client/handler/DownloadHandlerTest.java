package com.jefferson.regah.client.handler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadHandlerTest {
    @Test
    public void exceptionWhenMissingPathParameter() {
        final InvalidRequest exception = assertThrows(InvalidRequest.class, () -> new DownloadHandler(null)
                .act(Map.of(DownloadHandler.KEY_DATA, "")));
        assertThat("Exception message should note a parameter is missing", exception.getMessage(),
                allOf(containsString("Missing"), containsString(DownloadHandler.KEY_PATH)));
    }

    @Test
    public void exceptionWhenPathDoesntExist() throws IOException {
        final Path file = Files.createTempFile("file", "deleted");
        assertTrue(file.toFile().delete(), "Failed to delete a temp file that was created for the test");
        final InvalidRequest exception = assertThrows(InvalidRequest.class, () -> new DownloadHandler(null)
                .act(Map.of(DownloadHandler.KEY_DATA, "",
                        DownloadHandler.KEY_PATH, "/eeeee")));
        assertThat("Exception message should note path doesn't exist", exception.getMessage(),
                containsString("doesn't exist"));
    }

    @Test
    public void exceptionWhenMissingTransportationData() throws IOException {
        final Path file = Files.createTempFile("file", "temp");

        final InvalidRequest exception = assertThrows(InvalidRequest.class, () -> new DownloadHandler(null)
                .act(Map.of(DownloadHandler.KEY_PATH, file.toString())));
        assertThat("Exception message should note data parameter is missing", exception.getMessage(),
                allOf(containsString("Missing"), containsString(DownloadHandler.KEY_DATA)));
    }
}