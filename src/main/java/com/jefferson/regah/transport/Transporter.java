package com.jefferson.regah.transport;

import java.io.File;

public interface Transporter {
    TransportData getDownloadInfoFor(File file) throws FailureToPrepareForDownload;
}
