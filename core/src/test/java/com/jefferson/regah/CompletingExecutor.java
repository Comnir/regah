package com.jefferson.regah;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class CompletingExecutor {
    private static final Logger log = LogManager.getLogger(CompletingExecutor.class);

    private final ExecutorService executorService;
    private final CompletionService<Object> completionService;
    private final List<Future<Object>> futures;

    CompletingExecutor() {
        this.executorService = Executors.newCachedThreadPool();
        this.completionService = new ExecutorCompletionService<>(executorService);
        this.futures = new ArrayList<>(10);
    }

    void submit(Runnable runnable) {
        futures.add(completionService.submit(Executors.callable(runnable)));
    }

    void shutdownWhenComplete() throws InterruptedException {
        executorService.shutdown();

        for (int i = 0; i < futures.size(); i++) {
            final Future<Object> doneTask = completionService.take();
            try {
                doneTask.get();
            } catch (ExecutionException e) {
                log.error("An error was encountered in one of the tasks. Other tasks will be cancelled.", e);
                futures.forEach(future -> future.cancel(true));
            }
        }

        final int waitSeconds = 15;
        if (!executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS)) {
            log.info("executor didn't shutdown after " + waitSeconds + " seconds");
            executorService.shutdownNow();
            if (!executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS)) {
                log.warn("Some tasks didn't finish while trying to shutdown!");
            }
        }
    }
}
