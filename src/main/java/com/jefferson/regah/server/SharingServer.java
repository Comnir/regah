package com.jefferson.regah.server;

import com.jefferson.regah.SharedResources;
import com.jefferson.regah.server.handler.FetchResourceHandler;
import com.jefferson.regah.server.handler.ListResourcesHandler;
import com.jefferson.regah.server.transport.TorrentTransporter;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A sharing server provides the ability to list shared files and folders
 * and download them.
 */
public class SharingServer {
    private static final Logger log = LogManager.getLogger(SharingServer.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final int serverPort = 42424;
    private final SharedResources sharedResources;
    private final HttpServer httpServer;

    public SharingServer(SharedResources sharedResources) throws IOException {
        this.sharedResources = sharedResources;
        this.httpServer = HttpServer.create(new InetSocketAddress(serverPort), 10);
    }

    public void start() throws IOException {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::shutdown));

        httpServer.createContext("/listShared", new ListResourcesHandler(sharedResources));
        httpServer.createContext("/fetchResources", new FetchResourceHandler(sharedResources,
                new TorrentTransporter()));

        httpServer.setExecutor(executor);

        httpServer.start();

        log.info("Sharing server was started on port " + serverPort);
    }

    private void shutdown() {
        log.info("Shutting down server - will wait a bit for running requests to finish");
        log.debug("Stopping HTTP server.");
        httpServer.stop(10);

        log.debug("Shutting down the executor of the server requests.");
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    log.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
