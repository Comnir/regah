package com.jefferson.regah.client.handler;

import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.Transporter;
import com.jefferson.regah.transport.serialization.TransportDataDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

class DownloadHandlerTest {
    private Path temporaryFolder;

    @BeforeEach
    public void setup() throws IOException {
        temporaryFolder = Files.createTempDirectory("regah-test");
    }

    @Test
    public void exceptionWhenMissingPathParameter() {
        final InvalidRequest exception = assertThrows(InvalidRequest.class, () -> new DownloadHandler()
                .act(Map.of(DownloadHandler.KEY_DATA, "")));
        assertThat("Exception message should note a parameter is missing", exception.getMessage(),
                allOf(containsString("Missing"), containsString(DownloadHandler.KEY_PATH)));
    }

    @Test
    public void exceptionWhenPathDoesntExist() throws IOException {
        final Path file = Files.createTempFile("file", "deleted");
        assertTrue(file.toFile().delete(), "Failed to delete a temp file that was created for the test");
        final InvalidRequest exception = assertThrows(InvalidRequest.class, () -> new DownloadHandler()
                .act(Map.of(DownloadHandler.KEY_DATA, "",
                        DownloadHandler.KEY_PATH, "/eeeee")));
        assertThat("Exception message should note path doesn't exist", exception.getMessage(),
                containsString("doesn't exist"));
    }

    @Test
    public void exceptionWhenMissingTransportationData() throws IOException {
        final Path file = Files.createTempFile("file", "temp");
        final TransportDataDeserializer deserializer = mock(TransportDataDeserializer.class);
        final Function<String, TransportDataDeserializer> deserializerFactory = s -> deserializer;

        final InvalidRequest exception = assertThrows(InvalidRequest.class, () -> new DownloadHandler(deserializerFactory)
                .act(Map.of(DownloadHandler.KEY_PATH, file.toString())));
        assertThat("Exception message should note data parameter is missing", exception.getMessage(),
                allOf(containsString("Missing"), containsString(DownloadHandler.KEY_DATA)));
    }

    @Test
    public void transporterIsCalledWithTransportData() throws IOException {
        final TransportData transportData = mock(TransportData.class);
        final Transporter transporter = mock(Transporter.class);
        final TransportDataDeserializer deserializer = mock(TransportDataDeserializer.class);
        when(deserializer.getTransportData()).thenReturn(transportData);
        when(deserializer.getTransporter()).thenReturn(transporter);
        final Function<String, TransportDataDeserializer> deserializerFactory = s -> deserializer;
        final Path file = temporaryFolder.resolve("any.file" + UUID.randomUUID());
        assumeTrue(file.toFile().createNewFile(), "Failed to create a temp file for the test");

        new DownloadHandler(deserializerFactory)
                .act(Map.of(DownloadHandler.KEY_DATA, "Dummy data",
                        DownloadHandler.KEY_PATH, file.toString()));

        verify(transporter).downloadWithData(transportData, file);
    }
}