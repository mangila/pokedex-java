package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public record FetchNPokemons(PokeApiClient pokeApiClient,
                             QueueService queueService,
                             int count) implements Callable<List<Boolean>> {

    private static final Logger log = LoggerFactory.getLogger(FetchNPokemons.class);

    @Override
    public List<Boolean> call() throws Exception {

        var request = new JsonRequest(
                "GET",
                String.format("/api/v2/pokemon-species/?&limit=%d", count),
                List.of());
        return pokeApiClient.getJson(request)
                .map(PokeApiClientUtil::ensureSuccessStatusCode)
                .map(JsonResponse::getBody)
                .map(jsonTree -> jsonTree.getArray("results"))
                .map(array -> array.values().stream()
                        .map(JsonValue::getObject)
                        .map(jsonObject -> jsonObject.getString("url"))
                        .map(PokeApiUri::fromString)
                        .map(QueueEntry::new)
                        .peek(queueEntry -> log.debug("Queue entry {}", queueEntry.data()))
                        .map(queueEntry -> queueService.push(Application.POKEMON_SPECIES_URL_QUEUE, queueEntry)))
                .orElseThrow()
                .toList();
    }
}
