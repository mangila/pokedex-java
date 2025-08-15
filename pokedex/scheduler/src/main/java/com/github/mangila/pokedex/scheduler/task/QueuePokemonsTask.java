package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.client.response.PokemonsResponse;
import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record QueuePokemonsTask(PokeApiClient pokeApiClient,
                                QueueService queueService,
                                int pokemonLimit) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuePokemonsTask.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = VirtualThreadConfig.newSingleThreadScheduledExecutor();

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        var future = SCHEDULED_EXECUTOR.schedule(this, 1, TimeUnit.SECONDS);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        shutdown();
    }

    @Override
    public boolean shutdown() {
        var duration = Duration.ofSeconds(30);
        return VirtualThreadUtils.terminateExecutorGracefully(SCHEDULED_EXECUTOR, duration);
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
