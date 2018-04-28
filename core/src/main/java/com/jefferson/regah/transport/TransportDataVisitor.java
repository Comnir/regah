package com.jefferson.regah.transport;

import com.jefferson.regah.transport.torrent.TorrentTransportData;

public interface TransportDataVisitor {
    void visit(TorrentTransportData data);

    void visit(TransportData data);
}
