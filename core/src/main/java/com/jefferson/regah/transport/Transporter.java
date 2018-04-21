package com.jefferson.regah.transport;

import java.io.File;
import java.nio.file.Path;

public interface Transporter {
    TransportData dataForDownloading(File file) throws FailureToPrepareForDownload;

    void downloadWithData(TransportData data, Path destination);
}
