package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public record QueuePokemonsTask(PokeApiClient pokeApiClient, Queue queue, int pokemonLimit) implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueuePokemonsTask.class);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.submit(this);
    }

    @Override
    public void run() {
        try {
            if (pokemonLimit < 1) {
                throw new IllegalArgumentException("Pokemon limit must be greater than 0");
            }
            PokeApiUri uri = PokeApiUri.from("https://pokeapi.co/api/v2/pokemon-species?limit=" + pokemonLimit);
            pokeApiClient.fetchAsync(uri)
                    .thenApply(QueuePokemonsTask::getUris)
                    .thenApply(uriList -> uriList.stream().map(QueueEntry::new).toList())
                    .thenAccept(queueEntries -> queueEntries.forEach(queue::add))
                    .join();
        } catch (Exception e) {
            LOGGER.error("Failed to queue pokemons", e);
        }
    }

    private static List<PokeApiUri> getUris(JsonRoot jsonRoot) {
        return jsonRoot.getArray("results")
                .values()
                .stream()
                .map(JsonValue::unwrapObject)
                .map(jsonObject -> jsonObject.getString("url"))
                .map(PokeApiUri::from)
                .toList();
    }
}
