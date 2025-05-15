package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record InsertMediaTask(
        PokeApiClient pokeApiClient,
        QueueService queueService
) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(InsertMediaTask.class);

    @Override
    public void run() {
        try {
            log.debug("Fetching media");
        } catch (Exception e) {
            log.error("Error fetching media", e);
        }
    }
}
