package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public record InsertPokemonTask(PokeApiClient pokeApiClient,
                                QueueService queueService) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(InsertPokemonTask.class);

    @Override
    public void run() {
        var poll = queueService.poll(Application.POKEMON_SPECIES_URL_QUEUE);
        if (poll.isEmpty()) {
            log.info("Queue is empty");
            return;
        }
        var url = poll.get().getDataAsPokeApiUri();
        log.info("Queue entry {}", url);
        try {
            var pokemonSpecies = pokeApiClient.getJson(new JsonRequest("GET", url.getPath(), List.of()))
                    .map(PokeApiClientUtil::ensureSuccessStatusCode)
                    .map(JsonResponse::getBody)
                    .orElseThrow();

            var varieties = pokemonSpecies.getArray("varieties")
                    .values()
                    .stream()
                    .map(JsonValue::getObject)
                    .map(jsonObject -> jsonObject.getObject("pokemon"))
                    .map(jsonObject -> jsonObject.getString("url"))
                    .map(PokeApiUri::fromString)
                    .peek(pokeApiUri -> log.debug("variety path {}", pokeApiUri.getPath()))
                    .map(pokeApiUri -> pokeApiClient.getJsonAsync(new JsonRequest("GET", pokeApiUri.getPath(), List.of())))
                    .toList();

            var evolutionChain = Stream.of(pokemonSpecies.getObject("evolution_chain"))
                    .map(jsonObject -> jsonObject.getString("url"))
                    .map(PokeApiUri::fromString)
                    .peek(pokeApiUri -> log.debug("evolution chain path {}", pokeApiUri.getPath()))
                    .map(pokeApiUri -> pokeApiClient.getJsonAsync(new JsonRequest("GET", pokeApiUri.getPath(), List.of())))
                    .findFirst()
                    .orElseThrow();

            var parallel = CompletableFuture.allOf(
                    varieties.getFirst(),
                    varieties.getLast(),
                    evolutionChain
            );
            parallel.join();

//            varieties.stream()
//                    .map(CompletableFuture::join)
//                    .map(PokeApiClientUtil::ensureSuccessStatusCode)
//                    .map(JsonResponse::getBody)
//                    .map(JsonValue::getObject)
//                    .map(jsonObject -> jsonObject.getObject("pokemon"))
//                    .map(JsonValue::getString)
//                    .map(PokeApiUri::fromString)
//                    .forEach(queueService::push);

        } catch (Exception e) {
            var entry = poll.get();
            log.error("Error fetching pokemon species - {}", entry, e);
            queueService.add(Application.POKEMON_SPECIES_URL_QUEUE, entry);
        }
    }

}
