package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.FetchAllPokemonsTask;
import com.github.mangila.pokedex.scheduler.task.MediaTask;
import com.github.mangila.pokedex.scheduler.task.PokemonTask;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

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

    public void finishedProcessingTask(TaskConfig.TriggerConfig config) {
        log.info("Scheduling finished processing task");
        config.executor().scheduleAtFixedRate(() -> {
            if (queueService.isEmpty(Application.POKEMON_SPECIES_URL_QUEUE)
                    && queueService.isEmpty(Application.MEDIA_URL_QUEUE)) {
                log.debug("Queues is empty will shutdown");
                Application.IS_RUNNING.set(Boolean.FALSE);
            }
        }, config.initialDelay(), config.delay(), config.timeUnit());
    }

    public void mediaTask(TaskConfig config) {
        log.info("Scheduling media task");
        var task = new MediaTask(pokeApiClient, queueService);
        scheduleTask(config, () -> task);
    }

    public void pokemonTask(TaskConfig config) {
        log.info("Scheduling pokemon task");
        var task = new PokemonTask(pokeApiClient,queueService);
        scheduleTask(config, () -> task);
    }

    private void scheduleTask(TaskConfig config, Supplier<Runnable> task) {
        var triggerConfig = config.triggerConfig();
        var workerConfig = config.workerConfig();
        var workers = VirtualThreadConfig.newFixedThreadPool(workerConfig.poolSize());
        triggerConfig.executor()
                .scheduleAtFixedRate(() -> workers.submit(task.get()),
                        triggerConfig.initialDelay(),
                        triggerConfig.delay(),
                        triggerConfig.timeUnit());
    }
}
