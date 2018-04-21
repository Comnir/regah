package com.jefferson.regah.transport;

public interface TransportDataVisitor {
    void visit(TorrentTransportData data);

    void visit(TransportData data);
}
