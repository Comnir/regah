package com.jefferson.regah.transport.serialization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.transport.InvalidTransportData;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.UnsupportedTransportType;
import com.jefferson.regah.transport.torrent.TorrentTransportData;
import com.jefferson.regah.transport.torrent.TorrentTransportDeserializationTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransportDataDeserializerTest {
    private static final Gson gson = new Gson();

    private static Map<String, Object> validTorrentTransportDataJson() throws IOException {
        final String validJson;
        validJson = new String(Files.readAllBytes(Paths.get(
                TorrentTransportDeserializationTest.class.getResource("/torrent/validTorrentTransportData.json").getFile())),
                StandardCharsets.UTF_8);

        return gson.fromJson(validJson, TypeToken.getParameterized(Map.class, String.class, Object.class).getType());
    }

    @Test
    public void torrentTransportDataWhenTypeIsTorrent() throws IOException {
        final Map<String, Object> validData = validTorrentTransportDataJson();

        final TransportData transportData = new TransportDataDeserializer(json(Map.of(Common.TRANSPORT_TYPE_KEY, Common.TRANSPORT_TYPE_TORRENT_KEY,
                Common.TRANSPORT_DATA_KEY, json(validData))))
                .getTransportData();

        assertSame(TorrentTransportData.class, transportData.getClass(), "Incorrect class of TransportData");
    }

    @Test
    void exceptionWhenTransportTypeMissing() {
        final InvalidTransportData exception = assertThrows(InvalidTransportData.class, () ->
                new TransportDataDeserializer(json(Map.of())).getTransportData());
        assertThat("Exception message should note type parameter is unrecognized", exception.getMessage(),
                allOf(containsString("Missing"), containsString(Common.TRANSPORT_TYPE_KEY)));
    }

    @Test
    void exceptionWhenTransportTypeIsNotSupported() {
        final UnsupportedTransportType exception = assertThrows(UnsupportedTransportType.class, () ->
                new TransportDataDeserializer(json(Map.of(Common.TRANSPORT_TYPE_KEY, "Unknown_type")))
                        .getTransportData());
        assertThat("Exception message should note type parameter is missing", exception.getMessage(),
                allOf(containsString("type"), containsString("unknown")));
    }

    private String json(Object o) {
        return gson.toJson(o);
    }
}