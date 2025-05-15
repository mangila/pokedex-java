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
        var url = (PokeApiUri) poll.get().data();
        log.debug("Queue entry {}", url);
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
                .peek(pokeApiUri -> log.info("Queue entry {}", pokeApiUri.getPath()))
                .map(pokeApiUri -> pokeApiClient.getJsonAsync(new JsonRequest("GET", pokeApiUri.getPath(), List.of())))
                .toList();

        var evolutionChain = Stream.of(pokemonSpecies.getObject("evolution_chain"))
                .map(jsonObject -> jsonObject.getString("url"))
                .map(PokeApiUri::fromString)
                .map(pokeApiUri -> pokeApiClient.getJsonAsync(new JsonRequest("GET", pokeApiUri.getPath(), List.of())))
                .findFirst()
                .orElseThrow();

        var parallel = CompletableFuture.allOf(
                varieties.getFirst(),
                varieties.get(varieties.size() / 2),
                varieties.getLast(),
                evolutionChain
        );
        parallel.thenRun(() -> {
            log.info(evolutionChain.join().get().toString());
            log.info(varieties.get(0).join().get().toString());
        });


    }

}
