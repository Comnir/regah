package com.jefferson.regah.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final int serverPort;
    private final HttpServer httpServer;
    private final String description;

    public Server(int serverPort, String description) throws IOException {
        this.serverPort = serverPort;
        this.description = description;
        this.httpServer = HttpServer.create(new InetSocketAddress(this.serverPort), 10);
    }

    public void start(Map<String, HttpHandler> pathsWithHandlers) {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::stop));

        httpServer.setExecutor(executor);

        pathsWithHandlers.forEach(httpServer::createContext);

        httpServer.start();

        log.info(description + " started listening on port " + serverPort);
    }

    public void stop() {
        log.info("Stopping server - will wait a bit for running requests to finish");
        log.debug("Stopping HTTP server.");
        httpServer.stop(1);

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
