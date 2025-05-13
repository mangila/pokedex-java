package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.scheduler.model.PokeApiUri;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record FetchAllPokemonsTask(PokeApiClient pokeApiClient,
                                   QueueService queueService) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(FetchAllPokemonsTask.class);

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        var request = new JsonRequest(
                "GET",
                "/api/v2/pokemon-species/?&limit=1025",
                List.of());
        pokeApiClient.getJson()
                .andThen(Optional::orElseThrow)
                .andThen(PokeApiClientUtil::ensureSuccessStatusCode)
                .andThen(JsonResponse::body)
                .andThen(jsonBody -> jsonBody.getArray("results"))
                .andThen(array -> array.values().stream()
                        .map(JsonValue::getObject)
                        .map(jsonObject -> jsonObject.getString("url"))
                        .map(URI::create)
                        .map(PokeApiUri::new)
                        .map(QueueEntry::new))
                .apply(request)
                .peek(queueEntry -> log.debug(queueEntry.data().toString()))
                .forEach(queueEntry -> queueService.push(Application.POKEMON_SPECIES_URL_QUEUE, queueEntry));
    }
}
