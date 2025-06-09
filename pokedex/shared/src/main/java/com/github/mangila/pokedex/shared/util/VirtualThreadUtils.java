package com.github.mangila.pokedex.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class VirtualThreadUtils {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadUtils.class);

    private VirtualThreadUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean terminateExecutorGracefully(ExecutorService executorService, Duration awaitTermination) {
        try {
            executorService.shutdown();
            while (!executorService.awaitTermination(awaitTermination.toMillis(), TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Worker executor shutdown interrupted", e);
        }
        return executorService.isTerminated();
    }

}
