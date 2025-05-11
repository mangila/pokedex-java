package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PokemonTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);
    private final PokeApiClient pokeApiClient;
    private final QueueService queueService;

    public PokemonTask(PokeApiClient pokeApiClient,
                       QueueService queueService) {
        this.pokeApiClient = pokeApiClient;
        this.queueService = queueService;
    }

    @Override
    public void run() {
        try {
            var pokemonSpeciesUrl = queueService.poll("pokemon");
            log.debug("Fetching pokemon from {}", pokemonSpeciesUrl.get().data());
        } catch (Exception e) {
            log.error("Error fetching pokemon", e);
        }
    }

}
