package com.github.mangila.pokedex.app;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.Scheduler;
import com.github.mangila.pokedex.scheduler.SchedulerBootstrap;
import com.github.mangila.pokedex.scheduler.SchedulerConfig;
import com.github.mangila.pokedex.scheduler.task.*;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    public static final String POKEAPI_HOST = "pokeapi.co";
    public static final int POKEAPI_PORT = 443;
    public static final int POKEMON_LIMIT = 10;
    public static final String POKEMON_SPECIES_URL_QUEUE = "pokemon-species-url-queue";
    public static final String POKEMON_SPECIES_URL_DL_QUEUE = "pokemon-species-url-dl-queue";
    public static final String POKEMON_SPRITES_QUEUE = "pokemon-sprites-queue";
    public static final String POKEMON_CRIES_QUEUE = "pokemon-cries-queue";
    public static final boolean DELETE_DATABASE = Boolean.TRUE;
    public static final boolean TRUNCATE_DATABASE = Boolean.FALSE;

    public static void main(String[] args) {
        SchedulerBootstrap schedulerBootstrap = new SchedulerBootstrap();
        PokemonDatabase.defaultConfig();
        PokemonDatabase pokemonDatabase = PokemonDatabase.getInstance();
        PokeApiClient pokeApiClient = schedulerBootstrap.initPokeApiClient();
        schedulerBootstrap.configureQueues();
        QueueService queueService = QueueService.getInstance();
        List<Task> tasks = List.of(
                new QueuePokemonsTask(pokeApiClient, queueService, POKEMON_LIMIT),
                new InsertCriesTask(pokeApiClient, queueService),
                new InsertPokemonTask(pokeApiClient, queueService, pokemonDatabase),
                new InsertSpritesTask(pokeApiClient, queueService),
                new ShutdownTask(queueService)
        );
        Scheduler scheduler = new Scheduler(new SchedulerConfig(tasks));
        scheduler.init();
        while (Scheduler.IS_RUNNING.get()) {

        }
        LOGGER.info("Db size = {}", pokemonDatabase.db().size());
        scheduler.shutdownAllTasks();
        if (DELETE_DATABASE) {
            pokemonDatabase.db().delete();
        } else if (TRUNCATE_DATABASE) {
            pokemonDatabase.db().truncate();
        }
    }
}