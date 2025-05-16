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

public record QueuePokemonsTask(PokeApiClient pokeApiClient,
                                QueueService queueService,
                                int pokemonCount) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(QueuePokemonsTask.class);

    @Override
    public void run() {
        var request = new JsonRequest(
                "GET",
                String.format("/api/v2/pokemon-species/?&limit=%d", pokemonCount),
                List.of());
        pokeApiClient.getJson(request)
                .map(PokeApiClientUtil::ensureSuccessStatusCode)
                .map(JsonResponse::getBody)
                .map(jsonTree -> jsonTree.getArray("results"))
                .map(array -> array.values().stream()
                        .map(JsonValue::getObject)
                        .map(jsonObject -> jsonObject.getString("url"))
                        .map(PokeApiUri::fromString)
                        .map(QueueEntry::new)
                        .peek(queueEntry -> log.debug("Queue entry {}", queueEntry.data()))
                        .map(queueEntry -> queueService.add(Application.POKEMON_SPECIES_URL_QUEUE, queueEntry)))
                .orElseThrow()
                .toList();
    }
}
