package com.jefferson.regah.transport.serialization;

import com.google.gson.Gson;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.TransportDataVisitor;
import com.jefferson.regah.transport.torrent.TorrentTransportData;

import java.util.Map;

import static com.jefferson.regah.transport.serialization.Common.*;


/**
 * Serialize a {@link com.jefferson.regah.transport.TransportData} object.
 * The method of interest is {@link TransportDataSerializer#toJson(TransportData)}
 */
public class TransportDataSerializer implements TransportDataVisitor {
    private static final Gson gson = new Gson();
    private final ImmutableWrapper<String> json;

    public TransportDataSerializer() {
        json = new ImmutableWrapper<>();
    }

    @Override
    public void visit(TorrentTransportData data) {
        final Map map = Map.of(
                TRANSPORT_TYPE_KEY, TRANSPORT_TYPE_TORRENT_KEY,
                TRANSPORT_DATA_KEY, data.asJson(),
                TRANSPORT_ID_KEY, data.getId()
        );
        json.set(gson.toJson(map));
    }

    @Override
    public void visit(TransportData data) {

    }

    /**
     * Serialize the given object as json
     *
     * @param transportData object to be converted
     * @return JSON representation of the object
     */
    public String toJson(final TransportData transportData) {
        transportData.acceptVisitor(this);
        return json.asOptional()
                .orElseThrow(() -> new IllegalStateException("Internal error - failed to serialize transport data."));
    }
}
