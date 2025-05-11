package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.scheduler.model.PokeApiUri;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public record PokemonTask(PokeApiClient pokeApiClient,
                          QueueService queueService) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);

    @Override
    public void run() {
        var queueEntry = queueService.poll(Application.POKEMON_SPECIES_URL_QUEUE);
        if (queueEntry.isEmpty()) {
            log.debug("Queue is empty");
            return;
        }
        try {
            var pokemonSpeciesUrl = (PokeApiUri) queueEntry.get().data();
            var pokemonSpecies = pokeApiClient.getJson()
                    .apply(new JsonRequest("GET",
                            pokemonSpeciesUrl.getPath(),
                            List.of()));
            pokemonSpecies
                    .ifPresent(jsonResponse -> log.info("{}", jsonResponse.body().get("name")));
        } catch (Exception e) {
            log.error("ERR", e);
            queueService.push(Application.POKEMON_SPECIES_URL_QUEUE, queueEntry.get());
        }
    }

}
