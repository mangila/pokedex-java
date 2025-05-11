package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class FetchAllPokemonsTask implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(FetchAllPokemonsTask.class);
    private final PokeApiClient pokeApiClient;
    private final QueueService queueService;

    public FetchAllPokemonsTask(PokeApiClient pokeApiClient,
                                QueueService queueService) {
        this.pokeApiClient = pokeApiClient;
        this.queueService = queueService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Boolean call() throws Exception {
        var optionalJsonResponse = pokeApiClient.getJson()
                .apply(new JsonRequest("GET", "/api/v2/pokemon-species/?&limit=1025", List.of()));
        if (optionalJsonResponse.isEmpty()) {
            throw new IllegalStateException("Failed to fetch pokemons");
        }
        var response = optionalJsonResponse.get();
        log.debug("{}", response);
        if (response.httpStatus().code().startsWith("2")) {
            var result = (List<LinkedHashMap<String, Object>>) response
                    .body()
                    .get("results");
            result.stream()
                    .map(map -> (String) map.get("url"))
                    .forEach(url -> queueService.push("pokemon", new QueueEntry(url)));
        }
        return Boolean.TRUE;
    }
}
