package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record InsertSpritesTask(
        PokeApiClient pokeApiClient,
        QueueService queueService
) implements Task<Void> {

    private static final Logger log = LoggerFactory.getLogger(InsertSpritesTask.class);

    @Override
    public String getTaskName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Void call() throws Exception {
        try {
            log.debug("Fetching sprites");
        } catch (Exception e) {
            log.error("Error fetching sprites", e);
        }
        return null;
    }
}
