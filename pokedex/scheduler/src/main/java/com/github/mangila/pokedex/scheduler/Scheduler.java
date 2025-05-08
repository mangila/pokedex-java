package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private static final ScheduledExecutorService POKEMON_TASK = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    private static final ScheduledExecutorService MEDIA_TASK = VirtualThreadConfig.newSingleThreadScheduledExecutor();

    public static final AtomicBoolean isRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        log.info("Starting scheduler");
        isRunning.set(true);
        var queueService = new QueueService();
        queueService.createNewSetQueue("pokemon", 1024);
        queueService.createNewSetQueue("media", 1024);
        POKEMON_TASK.scheduleWithFixedDelay(new PokemonTask(queueService), 0, 1, TimeUnit.SECONDS);
        MEDIA_TASK.scheduleWithFixedDelay(new MediaTask(queueService), 0, 1, TimeUnit.SECONDS);

        while (isRunning.get()) {
        }
    }
}