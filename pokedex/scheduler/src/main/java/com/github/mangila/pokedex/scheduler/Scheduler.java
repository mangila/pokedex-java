package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.*;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.database.PokemonDatabase;
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
    private final PokemonDatabase pokemonDatabase;
    private final QueueService queueService;

    public Scheduler(PokeApiClient pokeApiClient,
                     PokeApiMediaClient mediaClient,
                     PokemonDatabase pokemonDatabase,
                     QueueService queueService) {
        this.pokeApiClient = pokeApiClient;
        this.mediaClient = mediaClient;
        this.pokemonDatabase = pokemonDatabase;
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
        config.executor().scheduleAtFixedRate(() -> {
            if (queueService.isEmpty(Application.POKEMON_SPECIES_URL_QUEUE)
                    && queueService.isEmpty(Application.POKEMON_SPRITES_QUEUE)) {
                log.debug("Queues is empty will shutdown");
                Application.IS_RUNNING.set(Boolean.FALSE);
            }
        }, config.initialDelay(), config.delay(), config.timeUnit());
    }

    public void scheduleInsertSprites(TaskConfig config) {
        scheduleTask(config, () -> new InsertSpritesTask(pokeApiClient, queueService));
    }

    public void scheduleInsertCries(TaskConfig config) {
        scheduleTask(config, () -> new InsertCriesTask(pokeApiClient, queueService));
    }

    public void scheduleInsertPokemons(TaskConfig config) {
        scheduleTask(config, () -> new InsertPokemonTask(pokeApiClient, queueService, pokemonDatabase));
    }

    private void scheduleTask(TaskConfig config, Supplier<Task<?>> task) {
        log.info("Scheduling {}", task.get().getTaskName());
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
