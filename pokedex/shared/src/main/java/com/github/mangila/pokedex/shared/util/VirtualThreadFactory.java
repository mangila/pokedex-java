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

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return Executors.newFixedThreadPool(nThreads, THREAD_FACTORY);
    }

    public static ExecutorService newSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(THREAD_FACTORY);
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
            LOGGER.error("Worker executor shutdown interrupted", e);
        }
        return executorService.isTerminated();
    }
}
