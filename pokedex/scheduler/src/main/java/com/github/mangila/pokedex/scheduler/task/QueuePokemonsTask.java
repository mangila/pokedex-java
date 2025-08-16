package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.client.response.PokemonsResponse;
import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

public record QueuePokemonsTask(PokeApiClient pokeApiClient,
                                QueueService queueService,
                                int pokemonLimit) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuePokemonsTask.class);
    private static final ExecutorService EXECUTOR = VirtualThreadFactory.newSingleThreadExecutor();

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        EXECUTOR.submit(this);
    }

    @Override
    public boolean shutdown() {
        var duration = Duration.ofSeconds(30);
        return VirtualThreadFactory.terminateExecutorGracefully(EXECUTOR, duration);
    }

    @Override
    public void run() {
        pokeApiClient.fetchAllPokemons(pokemonLimit)
                .thenApply(PokemonsResponse::uris)
                .thenApply(list -> list.stream().map(QueueEntry::new).toList())
                .thenAccept(queueEntries -> queueEntries.forEach(queueEntry -> {
                    queueService.add(
                            SchedulerApplication.POKEMON_SPECIES_URL_QUEUE,
                            queueEntry
                    );
                }))
                .join();
    }
}
