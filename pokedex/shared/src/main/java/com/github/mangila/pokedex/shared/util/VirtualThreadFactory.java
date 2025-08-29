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

    /**
     * Creates a new virtual thread pool with the given number of threads.
     * Virtual Thread pools are said to be an antipattern,
     * but you need them if you want to create a bound for Virtual-threads to actually be spawned.
     * <br>
     * <code>
     * for (int i = 0; i < Long.MAX_VALUE; i++) {
     * startThread();
     * }
     * </code>
     * <br>
     * Could easily return OutOfMemoryError if you don't limit the number of threads.
     * Virtual Threads are not for free, so you should consider how many to spawn
     *
     * @param nThreads the number of threads
     */
    public static ScheduledExecutorService newScheduledThreadPool(int nThreads) {
        return Executors.newScheduledThreadPool(nThreads, THREAD_FACTORY);
    }

    public static void terminateGracefully(ExecutorService executorService) {
        terminateGracefully(executorService, Duration.ofSeconds(5));
    }

    public static void terminateGracefully(ExecutorService executorService, Duration duration) {
        try {
            LOGGER.debug("Shutting down executor service {}", executorService);
            executorService.shutdown();
            while (!executorService.awaitTermination(duration.toMillis(), TimeUnit.MILLISECONDS)) {
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
