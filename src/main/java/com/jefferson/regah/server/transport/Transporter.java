package com.jefferson.regah.server.transport;

import java.io.File;

public interface Transporter {
    TransportData getCommunicationInfoFor(File file);
}
