package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record PokemonTask(PokeApiClient pokeApiClient,
                          QueueService queueService) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);

    @Override
    public void run() {
        var poll = queueService.poll(Application.POKEMON_SPECIES_URL_QUEUE);
        if (poll.isEmpty()) {
            log.debug("Queue is empty");
            return;
        }
        var queueEntry = poll.get();
        var url = (PokeApiUri) queueEntry.data();
        log.debug("Queue entry {}", url);
    }

}
