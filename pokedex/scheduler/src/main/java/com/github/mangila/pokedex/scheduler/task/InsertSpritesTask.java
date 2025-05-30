package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public record InsertSpritesTask(
        PokeApiClient pokeApiClient,
        QueueService queueService
) implements Task {

    private static final Logger log = LoggerFactory.getLogger(InsertSpritesTask.class);

    @Override
    public String getTaskName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public TaskConfig getTaskConfig() {
        var trigger = TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                TaskConfig.TaskType.FIXED_RATE,
                0,
                100,
                TimeUnit.MILLISECONDS);
        var workers = TaskConfig.WorkerConfig.from(10);
        return TaskConfig.from(trigger, workers);
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
