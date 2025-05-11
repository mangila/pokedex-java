package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaTask implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(MediaTask.class);
    private final PokeApiClient pokeApiClient;
    private final QueueService queueService;

    public MediaTask(PokeApiClient pokeApiClient,
                     QueueService queueService) {
        this.pokeApiClient = pokeApiClient;
        this.queueService = queueService;
    }

    @Override
    public void run() {
        try {
            log.debug("Fetching media");
        } catch (Exception e) {
            log.error("Error fetching media", e);
        }
    }
}
