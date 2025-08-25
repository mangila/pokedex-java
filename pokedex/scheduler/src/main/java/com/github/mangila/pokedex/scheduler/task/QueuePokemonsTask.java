package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;

public record QueuePokemonsTask(PokeApiClient pokeApiClient,
                                Queue queue,
                                int pokemonLimit) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuePokemonsTask.class);
    private static final ExecutorService EXECUTOR = VirtualThreadFactory.newSingleThreadExecutor();

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        LOGGER.info("Scheduling {}", name());
        EXECUTOR.submit(this);
    }

    @Override
    public boolean shutdown() {
        LOGGER.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        return VirtualThreadFactory.terminateGracefully(EXECUTOR, duration);
    }

    @Override
    public void run() {
        PokeApiUri uri = PokeApiUri.from("https://pokeapi.co/api/v2/pokemon-species?limit=" + pokemonLimit);
        pokeApiClient.fetch(uri)
                .thenApply(QueuePokemonsTask::getUris)
                .thenApply(list -> list.stream().map(QueueEntry::new).toList())
                .thenAccept(queueEntries -> queueEntries.forEach(queue::add))
                .join();
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
