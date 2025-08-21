package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.client.response.PokemonsResponse;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
        pokeApiClient.fetchAllPokemons(pokemonLimit)
                .thenApply(PokemonsResponse::uris)
                .thenApply(list -> list.stream().map(QueueEntry::new).toList())
                .thenAccept(queueEntries -> queueEntries.forEach(queue::add))
                .join();
    }
}
