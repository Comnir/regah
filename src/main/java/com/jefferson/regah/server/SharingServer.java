package com.jefferson.regah.server;

import com.jefferson.regah.SharedResources;
import com.jefferson.regah.server.handler.FetchResourceHandler;
import com.jefferson.regah.server.handler.ListResourcesHandler;
import com.jefferson.regah.server.transport.TorrentTransporter;
import com.jefferson.regah.server.transport.Transporter;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SharingServer {
    private static final Logger log = LogManager.getLogger(SharingServer.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final int serverPort = 42424;
    private final SharedResources sharedResources;

    public SharingServer(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    public void start() throws IOException {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::shutdown));

        final Transporter transporter = new TorrentTransporter();
        final HttpServer httpServer = HttpServer.create(new InetSocketAddress(serverPort), 10);

        httpServer.createContext("/listShared", new ListResourcesHandler(sharedResources));
        httpServer.createContext("/fetchResources", new FetchResourceHandler(sharedResources, transporter));

        httpServer.setExecutor(executor);

        httpServer.start();

        log.info("Sharing server was started on port " + serverPort);
    }

    private void shutdown() {
        log.info("Shutting down server - will wait a bit for running requests to finish");
        executor.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
    }
}
