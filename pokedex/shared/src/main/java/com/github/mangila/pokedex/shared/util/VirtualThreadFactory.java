package com.github.mangila.pokedex.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;

public class VirtualThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualThreadFactory.class);
    private static final String POKEDEX_VIRTUAL_THREAD_PREFIX = "pokedex-virtual-thread-";

    private static final ThreadFactory THREAD_FACTORY = Thread.ofVirtual()
            .name(POKEDEX_VIRTUAL_THREAD_PREFIX, 1)
            .factory();

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return Executors.newScheduledThreadPool(corePoolSize, THREAD_FACTORY);
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return Executors.newFixedThreadPool(nThreads, THREAD_FACTORY);
    }

    public static ExecutorService newSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(THREAD_FACTORY);
    }

    public static void terminateGracefully(ExecutorService executorService, Duration awaitTermination) {
        try {
            LOGGER.debug("Shutting down executor service {}", executorService);
            executorService.shutdown();
            while (!executorService.awaitTermination(awaitTermination.toMillis(), TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
            LOGGER.debug("Executor service {} terminated", executorService);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for termination", e);
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}
