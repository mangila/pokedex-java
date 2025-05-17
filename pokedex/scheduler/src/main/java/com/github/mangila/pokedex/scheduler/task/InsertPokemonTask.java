package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.model.PokemonMapper;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
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
        var url = poll.get().getDataAs(PokeApiUri.class);
        log.info("Queue entry {}", url);
        try {
            var pokemonSpecies = pokeApiClient.getJson(new JsonRequest("GET", url.getPath(), List.of()))
                    .map(PokeApiClientUtil::ensureSuccessStatusCode)
                    .map(JsonResponse::body)
                    .orElseThrow();

            var evolutionChain = Stream.of(pokemonSpecies.getObject("evolution_chain"))
                    .map(jsonObject -> jsonObject.getString("url"))
                    .map(PokeApiUri::fromString)
                    .peek(pokeApiUri -> log.info("evolution chain path {}", pokeApiUri.getPath()))
                    .map(pokeApiUri -> pokeApiClient.getJsonAsync(new JsonRequest(
                                    "GET",
                                    pokeApiUri.getPath(),
                                    List.of()))
                            .thenApply(jsonResponse -> jsonResponse
                                    .stream()
                                    .peek(jsonTree -> log.debug("evolution chain response {}", jsonTree))
                                    .map(PokeApiClientUtil::ensureSuccessStatusCode)
                                    .map(JsonResponse::body)
                                    .map(PokemonMapper::toPokemonEvolutionChain)
                                    .findFirst()
                                    .orElseThrow()))
                    .map(CompletableFuture::join)
                    .findFirst()
                    .orElseThrow();

            var varietyFutures = pokemonSpecies.getArray("varieties")
                    .values()
                    .stream()
                    .map(JsonValue::getObject)
                    .map(jsonObject -> jsonObject.getObject("pokemon"))
                    .map(jsonObject -> jsonObject.getString("url"))
                    .map(PokeApiUri::fromString)
                    .peek(pokeApiUri -> log.debug("variety path {}", pokeApiUri.getPath()))
                    .map(pokeApiUri -> pokeApiClient.getJsonAsync(new JsonRequest(
                                    "GET",
                                    pokeApiUri.getPath(),
                                    List.of()))
                            .thenApply(jsonResponse -> jsonResponse
                                    .stream()
                                    .peek(jsonTree -> log.debug("variety response {}", jsonTree))
                                    .map(PokeApiClientUtil::ensureSuccessStatusCode)
                                    .map(JsonResponse::body)
                                    .findFirst()
                                    .orElseThrow())
                            .thenApply(jsonTree -> {
                                log.debug("Running side effect put sprites to queue");
                                var sprites = jsonTree.getObject("sprites");
                                queueService.add(Application.POKEMON_SPRITES_QUEUE, new QueueEntry(sprites));
                                return jsonTree;
                            })
                            .thenApply(jsonTree -> {
                                log.debug("Running side effect put cries to queue");
                                var sprites = jsonTree.getObject("cries");
                                queueService.add(Application.POKEMON_CRIES_QUEUE, new QueueEntry(sprites));
                                return jsonTree;
                            })
                            .thenApply(PokemonMapper::toPokemonVariety))
                    .toList();

            CompletableFuture.allOf(varietyFutures.toArray(CompletableFuture[]::new))
                    .join();

            var pokemonVarieties = varietyFutures.stream()
                    .map(CompletableFuture::join)
                    .peek(pokemonVariety -> log.debug("pokemon variety {} from species - {}", pokemonVariety.getName(), pokemonSpecies
                            .getValue("name")
                            .getString()))
                    .toList();

            var pokemon = PokemonMapper.toPokemon(
                    pokemonSpecies,
                    pokemonVarieties,
                    null);

        } catch (Exception e) {
            var entry = poll.get();
            if (entry.shouldNotBeProcessed(3)) {
                log.info("{} - should not be processed again", entry.data());
                return;
            }
            entry.incrementFailCounter();
            log.error("Error fetching pokemon species - {}", entry, e);
            queueService.add(Application.POKEMON_SPECIES_URL_QUEUE, entry);
        }
    }

}
