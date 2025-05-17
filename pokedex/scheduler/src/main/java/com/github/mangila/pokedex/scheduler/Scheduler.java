package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.InsertCriesTask;
import com.github.mangila.pokedex.scheduler.task.InsertPokemonTask;
import com.github.mangila.pokedex.scheduler.task.InsertSpritesTask;
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
            var queueSize = executor.submit(task)
                    .get();
            log.info("Queued {} pokemons", queueSize);
        } catch (Exception e) {
            log.error("Error fetching all pokemons", e);
        }
    }

    public void scheduleFinishedProcessing(TaskConfig.TriggerConfig config) {
        log.info("Scheduling finished processing task");
        config.executor().scheduleAtFixedRate(() -> {
            if (queueService.isEmpty(Application.POKEMON_SPECIES_URL_QUEUE)
                    && queueService.isEmpty(Application.POKEMON_SPRITES_QUEUE)) {
                log.debug("Queues is empty will shutdown");
                Application.IS_RUNNING.set(Boolean.FALSE);
            }
        }, config.initialDelay(), config.delay(), config.timeUnit());
    }

    public void scheduleInsertSprites(TaskConfig config) {
        log.info("Scheduling insert sprites task");
        var task = new InsertSpritesTask(pokeApiClient, queueService);
        scheduleTask(config, () -> task);
    }

    public void scheduleInsertCries(TaskConfig config) {
        log.info("Scheduling insert cries task");
        var task = new InsertCriesTask(pokeApiClient, queueService);
        scheduleTask(config, () -> task);
    }

    public void scheduleInsertPokemons(TaskConfig config) {
        log.info("Scheduling insert pokemons task");
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
