package com.jefferson.regah.transport.torrent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.serialization.TransportDataDeserializer;
import com.jefferson.regah.transport.serialization.TransportDataSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TorrentTransportDeserializationTest {
    private static final Gson gson = new Gson();
    private static Map<String, Object> validData;
    private static String validJson;

    private static Map<String, Object> validTorrentTransportDataJson() throws IOException {
        validJson = new String(Files.readAllBytes(Paths.get(
                TorrentTransportDeserializationTest.class.getResource("/torrent/validTorrentTransportData.json").getFile())),
                StandardCharsets.UTF_8);

        return gson.fromJson(validJson, TypeToken.getParameterized(Map.class, String.class, Object.class).getType());
    }

    @BeforeAll
    static void setupAll() throws IOException {
        validData = Collections.unmodifiableMap(validTorrentTransportDataJson());
    }

    @Test
    void exceptionWhenTransportDataIsMissingId() {
        assertThrows(NullPointerException.class, () -> TorrentTransportData.
                fromJson(jsonOfValidDataWithout(TorrentTransportData.ID)));
    }

    @Test
    void exceptionWhenTransportDataIsMissingSeedingPeerDto() {
        assertThrows(NullPointerException.class, () -> TorrentTransportData.
                fromJson(jsonOfValidDataWithout(TorrentTransportData.SEEDING_PEER_DTO)));
    }

    @Test
    void exceptionWhenTransportDataIsMissingTorrentData() {
        assertThrows(NullPointerException.class, () -> TorrentTransportData.
                fromJson(jsonOfValidDataWithout(TorrentTransportData.TORRENT_DATA)));
    }

    @Test
    void deserializedToCrrectTypes() {
        final String jsonFromSerializer = new TransportDataSerializer().toJson(TorrentTransportData.fromJson(validJson));

        final TransportDataDeserializer deserializer = new TransportDataDeserializer(jsonFromSerializer);
        assertTrue(deserializer.getTransportData() instanceof TorrentTransportData,
                "Deserializing torrent transport data should return an instance of TorrentTransportData.");
        assertTrue(deserializer.getTransporter() instanceof TorrentTransporter,
                "Deserializing torrent transport data should return an instance of TorrentTransporter.");
    }


    @Test
    void deserializedToObjectWithCorrectValues() {
        final TorrentTransportData data = TorrentTransportData.fromJson(validJson);
        assertEquals("id-string", data.getId(), "ID not deserialized to correct value.");
        assertArrayEquals(Base64.getDecoder().decode("dataAsByteArray"), data.getTorrentData(),
                "TorrentData byte array not deserialized to correct value.");
        assertEquals(12345, data.getSeedingPeer().getPort(), "seeding-peer port not deserialized to correct value.");
    }

    @Test
    void serializerInvertsDeserializer() {
        final TorrentTransportData deserializedFromOriginal = TorrentTransportData.fromJson(validJson);
        final String jsonFromSerializer = new TransportDataSerializer().toJson(deserializedFromOriginal);
        final TransportData deserializedFromSerialized = new TransportDataDeserializer(jsonFromSerializer).getTransportData();

        assertEquals(deserializedFromOriginal, deserializedFromSerialized,
                "TransportData from original JSON and from serialized JSON should be equal.");
    }

    private String json(Object o) {
        return gson.toJson(o);
    }

    private String jsonOfValidDataWithout(final String key) {
        return json(withoutKey(validData, key));
    }

    private Map<String, Object> withoutKey(final Map<String, Object> original, final String key) {
        final Map<String, Object> updated = new HashMap<>(original);
        updated.remove(key);
        return updated;
    }
}
