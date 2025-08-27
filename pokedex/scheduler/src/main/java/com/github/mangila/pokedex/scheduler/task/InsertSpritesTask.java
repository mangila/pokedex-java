package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record InsertSpritesTask(
        PokeApiClient pokeApiClient,
        Queue defaultQueue
) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSpritesTask.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_POOL = VirtualThreadFactory.newScheduledThreadPool(10);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        LOGGER.info("Scheduling {}", name());
        SCHEDULED_EXECUTOR_POOL.scheduleAtFixedRate(this,
                100,
                100,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        VirtualThreadFactory.terminateGracefully(SCHEDULED_EXECUTOR_POOL, duration);
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("Fetching sprites");
        } catch (Exception e) {
            LOGGER.error("Error fetching sprites", e);
        }
    }
}
