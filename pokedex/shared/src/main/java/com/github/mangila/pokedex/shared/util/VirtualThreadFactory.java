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
            .uncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in thread {}", t.getName(), e))
            .factory();

    /**
     * An {@link ExecutorService} that creates a new virtual thread for each task submitted.
     * This executor is suitable for workloads where tasks are short-lived and highly concurrent,
     * leveraging the efficient resource handling of virtual threads.
     */
    public static final ExecutorService THREAD_PER_TASK_EXECUTOR = Executors.newThreadPerTaskExecutor(THREAD_FACTORY);

    /**
     * Creates a single-threaded scheduled executor service using a virtual thread factory.
     * The returned executor is suitable for tasks that require a single worker thread
     * that executes tasks sequentially in a scheduled manner.
     *
     * @return a ScheduledExecutorService that uses a single virtual thread for executing tasks
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
    }


    /**
     * Creates a scheduled thread pool with a specified number of virtual threads.
     * <p>
     * This is considered an antipattern,
     * but sometimes you need some kind of bound for the thread spawning
     * </p>
     *
     * @param nThreads the number of threads in the pool
     * @return a ScheduledExecutorService backed by a thread pool with the specified number of threads
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
