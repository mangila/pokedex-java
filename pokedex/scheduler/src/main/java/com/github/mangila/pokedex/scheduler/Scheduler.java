package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.InsertMediaTask;
import com.github.mangila.pokedex.scheduler.task.InsertPokemonTask;
import com.github.mangila.pokedex.scheduler.task.QueuePokemonsTask;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiMediaClient;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private final PokeApiClient pokeApiClient;
    private final PokeApiMediaClient mediaClient;
    private final QueueService queueService;

    public Scheduler(PokeApiClient pokeApiClient,
                     PokeApiMediaClient mediaClient,
                     QueueService queueService) {
        this.pokeApiClient = pokeApiClient;
        this.mediaClient = mediaClient;
        this.queueService = queueService;
    }

    public void queuePokemons(ExecutorService executor, int pokemonCount) {
        try {
            log.info("Scheduling fetch all pokemons task");
            var task = new QueuePokemonsTask(
                    pokeApiClient,
                    queueService,
                    pokemonCount);
            var future = executor.submit(task)
                    .get();
            if (future == null) {
                log.error("Error fetching all pokemons");
            }
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

    public void insertMedia(TaskConfig config) {
        log.info("Scheduling media task");
        var task = new InsertMediaTask(pokeApiClient, queueService);
        scheduleTask(config, () -> task);
    }

    public void insertPokemons(TaskConfig config) {
        log.info("Scheduling pokemon task");
        var task = new InsertPokemonTask(pokeApiClient, queueService);
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
