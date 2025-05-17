package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record InsertCriesTask(
        PokeApiClient pokeApiClient,
        QueueService queueService
) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(InsertCriesTask.class);

    @Override
    public void run() {
        try {
            log.debug("Fetching cries");
        } catch (Exception e) {
            log.error("Error fetching cries", e);
        }
    }
}
