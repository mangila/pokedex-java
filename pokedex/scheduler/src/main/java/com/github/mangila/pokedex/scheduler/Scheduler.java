package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {


    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private final PokeApiClient pokeApiClient;
    private final QueueService queueService;

    public Scheduler(PokeApiClient pokeApiClient,
                     QueueService queueService) {
        this.pokeApiClient = pokeApiClient;
        this.queueService = queueService;
    }

    public void fetchAllPokemonsTask(ExecutorService executor) {
        try {
            log.info("Scheduling fetch all pokemons task");
            var future = executor.submit(new FetchAllPokemonsTask(pokeApiClient, queueService));
            future.get();
        } catch (Exception e) {
            log.error("Error fetching all pokemons", e);
        }
    }

    public void mediaTask(ScheduledExecutorService executor) {
        log.info("Scheduling media task");
        executor.scheduleAtFixedRate(
                new MediaTask(pokeApiClient, queueService),
                1,
                1,
                TimeUnit.SECONDS);
    }

    public void pokemonTask(ScheduledExecutorService executor) {
        log.info("Scheduling pokemon task");
        executor.scheduleWithFixedDelay(
                new PokemonTask(pokeApiClient, queueService),
                1,
                1,
                TimeUnit.SECONDS);
    }
}
