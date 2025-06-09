package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record InsertSpritesTask(
        PokeApiClient pokeApiClient,
        QueueService queueService
) implements Task {

    private static final Logger log = LoggerFactory.getLogger(InsertSpritesTask.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    private static final ExecutorService WORKER_POOL = VirtualThreadConfig.newFixedThreadPool(10);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> WORKER_POOL.submit(this),
                100,
                100,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean shutdown() {
        log.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        return VirtualThreadUtils.terminateExecutorGracefully(SCHEDULED_EXECUTOR, duration) &&
                VirtualThreadUtils.terminateExecutorGracefully(WORKER_POOL, duration);
    }

    @Override
    public void run() {
        try {
            log.debug("Fetching sprites");
        } catch (Exception e) {
            log.error("Error fetching sprites", e);
        }
    }
}
