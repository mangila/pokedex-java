package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.FetchAllPokemonsTask;
import com.github.mangila.pokedex.scheduler.task.MediaTask;
import com.github.mangila.pokedex.scheduler.task.PokemonTask;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

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
            var task = new FetchAllPokemonsTask(pokeApiClient, queueService);
            var future = executor.submit(task);
            future.get();
        } catch (Exception e) {
            log.error("Error fetching all pokemons", e);
        }
    }

    public void mediaTask(ScheduledExecutorService executor,
                          ScheduledConfig config) {
        log.info("Scheduling media task");
        var task = new MediaTask(pokeApiClient, queueService);
        executor.scheduleAtFixedRate(
                task,
                config.initialDelay(),
                config.delay(),
                config.timeUnit());
    }

    public void pokemonTask(ScheduledExecutorService executor,
                            ScheduledConfig config) {
        log.info("Scheduling pokemon task");
        var task = new PokemonTask(pokeApiClient, queueService);
        executor.scheduleWithFixedDelay(
                task,
                config.initialDelay(),
                config.delay(),
                config.timeUnit());
    }

    public void finishedProcessing(ScheduledExecutorService executor,
                                   ScheduledConfig config) {
        executor.scheduleWithFixedDelay(() -> {
            if (queueService.isEmpty(Application.POKEMON_SPECIES_URL_QUEUE)
                    && queueService.isEmpty(Application.MEDIA_URL_QUEUE)) {
                log.debug("Shutting down scheduler");
                Application.isRunning.set(Boolean.FALSE);
            }
        }, config.delay(), config.delay(), config.timeUnit());
    }
}
