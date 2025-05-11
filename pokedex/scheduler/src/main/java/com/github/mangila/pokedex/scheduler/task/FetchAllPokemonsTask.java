package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.scheduler.model.PokeApiUri;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

public record FetchAllPokemonsTask(PokeApiClient pokeApiClient,
                                   QueueService queueService) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(FetchAllPokemonsTask.class);

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        var optionalJsonResponse = pokeApiClient.getJson()
                .apply(new JsonRequest("GET", "/api/v2/pokemon-species/?&limit=1025", List.of()));
        if (optionalJsonResponse.isEmpty()) {
            throw new IllegalStateException("Failed to fetch pokemons");
        }
        var response = optionalJsonResponse.get();
        log.debug("{}", response);
        if (response.httpStatus().code().startsWith("2")) {
            var results = (List<LinkedHashMap<String, Object>>) response
                    .body()
                    .get("results");
            results.stream()
                    .map(map -> (String) map.get("url"))
                    .map(URI::create)
                    .map(PokeApiUri::new)
                    .map(QueueEntry::new)
                    .forEach(queueEntry -> queueService.push(Application.POKEMON_SPECIES_URL_QUEUE, queueEntry));
        }
    }
}
