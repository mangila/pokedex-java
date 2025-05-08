package com.github.mangila.pokedex.shared.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class VirtualThreadConfig {

    private static final ThreadFactory THREAD_FACTORY = Thread.ofVirtual()
            .name("pokedex-virtual-thread-",1)
            .factory();

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
    }
}
