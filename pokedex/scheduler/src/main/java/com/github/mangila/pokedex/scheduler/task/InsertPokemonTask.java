package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.PokemonDatabase;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.model.PokemonMapper;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiUri;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public record InsertPokemonTask(PokeApiClient pokeApiClient,
                                QueueService queueService,
                                PokemonDatabase database) implements Task {

    private static final Logger log = LoggerFactory.getLogger(InsertPokemonTask.class);

    @Override
    public String getTaskName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public TaskConfig getTaskConfig() {
        var trigger = TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                TaskConfig.TaskType.FIXED_RATE,
                0,
                100,
                TimeUnit.MILLISECONDS);
        var workers = TaskConfig.WorkerConfig.from(10);
        return TaskConfig.from(trigger, workers);
    }

    /**
     * Executes the task of processing Pokémon species data by interacting with the Pokémon API and internal services.
     * The method performs the following steps:
     *
     * 1. Polls the Pokémon species URL queue for an entry.
     *    - If the queue is empty, logs the status and exits.
     *    - Extracts and validates the URL from the polled entry.
     *
     * 2. Fetches the Pokémon species details from the Pokémon API.
     *    - Retrieves the JSON response and validates its success status.
     *    - Processes the "evolution_chain" data, fetching and mapping it into an internal representation.
     *
     * 3. Processes all Pokémon varieties associated with the species.
     *    - Asynchronously fetches data for each variety from the Pokémon API.
     *    - Maps the fetched JSON into internal representations and handles side effects, such as:
     *       - Adding sprite data to the Pokémon sprites queue.
     *       - Adding cries data to the Pokémon cries queue.
     *
     * 4. Constructs the Pokémon object from the species, varieties, and evolution chain data.
     *    - Persists the Pokémon object to the database asynchronously.
     *    - Logs success or failure of the persistence operation.
     *
     * 5. Handles errors during API interaction or data processing.
     *    - Retries or requeues entries with incremental failure counters.
     *    - Logs errors and updates queue entries as needed.
     */
    @Override
    public void run() {
        var poll = queueService.poll(SchedulerApplication.POKEMON_SPECIES_URL_QUEUE);
        if (poll.isEmpty()) {
            log.debug("Queue is empty");
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
                                var name = jsonTree.getValue("name");
                                log.debug("Running side effect put - {} sprites to queue", name);
                                var sprites = jsonTree.getObject("sprites");
                                sprites.add("pokemon_id", pokemonSpecies.getValue("id"));
                                sprites.add("name", name);
                                sprites.add("variety_id", jsonTree.getValue("id"));
                                queueService.add(SchedulerApplication.POKEMON_SPRITES_QUEUE, new QueueEntry(sprites));
                                return jsonTree;
                            })
                            .thenApply(jsonTree -> {
                                var name = jsonTree.getValue("name");
                                log.debug("Running side effect put - {} cries to queue", name);
                                var cries = jsonTree.getObject("cries");
                                cries.add("pokemon_id", pokemonSpecies.getValue("id"));
                                cries.add("name", name);
                                cries.add("variety_id", jsonTree.getValue("id"));
                                queueService.add(SchedulerApplication.POKEMON_CRIES_QUEUE, new QueueEntry(cries));
                                return jsonTree;
                            })
                            .thenApply(PokemonMapper::toPokemonVariety))
                    .toList();

            CompletableFuture.allOf(varietyFutures.toArray(CompletableFuture[]::new))
                    .join();

            var pokemonVarieties = varietyFutures.stream()
                    .map(CompletableFuture::join)
                    .peek(pokemonVariety -> log.debug("pokemon variety {} from species - {}", pokemonVariety.name(), pokemonSpecies
                            .getValue("name")
                            .getString()))
                    .toList();

            var pokemon = PokemonMapper.toPokemon(
                    pokemonSpecies,
                    pokemonVarieties,
                    evolutionChain);
            boolean ok = database.get().
                    putAsync(pokemon.name(), pokemon)
                    .join();
            if (ok) {
                log.info("Pokemon {} has been successfully added", pokemon.name());
            } else {
                throw new IllegalStateException("Failed to add Pokemon " + pokemon.name() + " to database");
            }
        } catch (Exception e) {
            var entry = poll.get();
            if (entry.equalsMaxRetries(3)) {
                queueService.add(SchedulerApplication.POKEMON_SPECIES_URL_DL_QUEUE, entry);
                return;
            }
            entry.incrementFailCounter();
            log.error("Error fetching pokemon species - {}", entry, e);
            queueService.add(SchedulerApplication.POKEMON_SPECIES_URL_QUEUE, entry);
        }
    }
}
