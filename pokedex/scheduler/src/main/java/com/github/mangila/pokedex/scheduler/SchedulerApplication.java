package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.scheduler.task.*;
import com.github.mangila.pokedex.shared.queue.QueueService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class SchedulerApplication {

    public static final String POKEAPI_HOST = "pokeapi.co";
    public static final int POKEAPI_PORT = 443;
    public static final int POKEMON_LIMIT = 1;
    public static final String POKEMON_SPECIES_URL_QUEUE = "pokemon-species-url-queue";
    public static final String POKEMON_SPECIES_URL_DL_QUEUE = "pokemon-species-url-dl-queue";
    public static final String POKEMON_SPRITES_QUEUE = "pokemon-sprites-queue";
    public static final String POKEMON_CRIES_QUEUE = "pokemon-cries-queue";
    public static final DatabaseName POKEMON_DATABASE_NAME = new DatabaseName("pokedex");
    public static final boolean DELETE_DATABASE = Boolean.TRUE;
    public static final boolean TRUNCATE_DATABASE = Boolean.FALSE;
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(Boolean.FALSE);

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        PokeApiClient pokeApiClient = bootstrap.initPokeApiClient();
        QueueService queueService = bootstrap.initQueueService();
        PokemonDatabase pokemonDatabase = bootstrap.initPokemonDatabase(POKEMON_DATABASE_NAME);
        pokemonDatabase.instance().init();
        List<Task> tasks = List.of(
                new QueuePokemonsTask(pokeApiClient, queueService, POKEMON_LIMIT),
                new InsertCriesTask(pokeApiClient, queueService),
                new InsertPokemonTask(pokeApiClient, queueService, pokemonDatabase),
                new InsertSpritesTask(pokeApiClient, queueService),
                new ShutdownTask(queueService)
        );
        Scheduler scheduler = new Scheduler(new SchedulerConfig(tasks));
        scheduler.init();
        IS_RUNNING.set(Boolean.TRUE);
        while (IS_RUNNING.get()) {

        }
        scheduler.shutdownAllTasks();
        if (DELETE_DATABASE) {
            pokemonDatabase.instance().deleteDatabase();
        } else if (TRUNCATE_DATABASE) {
            pokemonDatabase.instance().truncateDatabase();
        }
    }
}