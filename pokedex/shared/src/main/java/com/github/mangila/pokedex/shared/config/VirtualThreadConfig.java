package com.github.mangila.pokedex.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class VirtualThreadConfig {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadConfig.class);

    private static final ThreadFactory THREAD_FACTORY = Thread.ofVirtual()
            .name("pokedex-virtual-thread-", 1)
            .factory();

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return Executors.newFixedThreadPool(nThreads, THREAD_FACTORY);
    }

    public static ExecutorService newVirtualThreadPerTaskExecutor() {
        return Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
    }
}
