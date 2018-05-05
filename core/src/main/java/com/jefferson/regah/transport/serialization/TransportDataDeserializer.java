package com.jefferson.regah.transport.serialization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jefferson.regah.com.jefferson.jade.ImmutableWrapper;
import com.jefferson.regah.transport.InvalidTransportData;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.Transporter;
import com.jefferson.regah.transport.UnsupportedTransportType;
import com.jefferson.regah.transport.torrent.TorrentTransportData;
import com.jefferson.regah.transport.torrent.TorrentTransporter;

import java.util.Map;

import static com.jefferson.regah.transport.serialization.Common.*;

public class TransportDataDeserializer {
    private static final Gson gson = new Gson();

    private final String transportDataJson;
    private final ImmutableWrapper<TransportData> transportDataWrapper;
    private final ImmutableWrapper<Transporter> transporterWrapper;

    public TransportDataDeserializer(final String transportDataJson) {
        this.transportDataJson = transportDataJson;
        transporterWrapper = new ImmutableWrapper<>();
        transportDataWrapper = new ImmutableWrapper<>();
    }

    public TransportData getTransportData() throws UnsupportedTransportType, InvalidTransportData {
        init();
        return transportDataWrapper.get();
    }

    public Transporter getTransporter() throws UnsupportedTransportType, InvalidTransportData {
        init();
        return transporterWrapper.get();
    }

    private void init()
            throws InvalidTransportData, UnsupportedTransportType {
        if (transporterWrapper.isPresent() || transportDataWrapper.isPresent()) {
            return;
        }

        final Map<String, String> map = gson.fromJson(transportDataJson,
                TypeToken.getParameterized(Map.class, String.class, String.class).getType());

        final String transportType = map.getOrDefault(TRANSPORT_TYPE_KEY, "");

        if (transportType.isEmpty()) {
            throw new InvalidTransportData("Transport data is mising a type.");
        }

        switch (transportType) {
            case TRANSPORT_TYPE_TORRENT_KEY:
                transporterWrapper.set(new TorrentTransporter());
                transportDataWrapper.set(gson.fromJson(map.get(TRANSPORT_DATA_KEY), TorrentTransportData.class));
                break;
            default:
                throw new UnsupportedTransportType(String.format("Provided transport data type is unknown" +
                        " and won't be handled. Type: %s", transportType));
        }
    }


}
