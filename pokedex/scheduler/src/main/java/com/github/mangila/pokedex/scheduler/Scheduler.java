package com.github.mangila.pokedex.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private static final ScheduledExecutorService POKEMON_TASK = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
    private static final ScheduledExecutorService MEDIA_TASK = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

    public static final AtomicBoolean isRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        log.info("Starting scheduler");
        isRunning.set(true);
        POKEMON_TASK.scheduleWithFixedDelay(() -> log.debug("pokemon-task"), 0, 1, TimeUnit.SECONDS);
        MEDIA_TASK.scheduleWithFixedDelay(() -> log.debug("media-task"), 0, 1, TimeUnit.SECONDS);
        while (isRunning.get()) {
        }

    }
}