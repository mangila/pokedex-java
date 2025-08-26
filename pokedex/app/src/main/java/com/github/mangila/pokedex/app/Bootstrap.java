package com.github.mangila.pokedex.app;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.Scheduler;
import com.github.mangila.pokedex.scheduler.SchedulerConfig;
import com.github.mangila.pokedex.scheduler.task.*;
import com.github.mangila.pokedex.shared.queue.QueueService;

import java.util.List;

import static com.github.mangila.pokedex.shared.Config.*;

public final class Bootstrap {

    public void configurePokemonDatabase() {
        PokemonDatabase.configureDefaultSettings();
    }

    public void configurePokeApiClient() {
        PokeApiClient.configureDefaultSettings();
    }

    public Scheduler initScheduler() {
        QueueService queueService = QueueService.getInstance();
        PokeApiClient pokeApiClient = PokeApiClient.getInstance();
        PokemonDatabase pokemonDatabase = PokemonDatabase.getInstance();
        pokemonDatabase.instance().open();
        List<Task> tasks = List.of(
                new QueuePokemonsTask(pokeApiClient, queueService.getQueue(POKEMON_SPECIES_URL_QUEUE), POKEMON_LIMIT),
                new InsertCriesTask(pokeApiClient, queueService.getQueue(POKEMON_CRIES_QUEUE)),
                new InsertSpeciesResponseTaskTask(pokeApiClient, queueService.getQueue(POKEMON_SPECIES_URL_QUEUE), pokemonDatabase),
                new InsertSpritesTask(pokeApiClient, queueService.getQueue(POKEMON_SPRITES_QUEUE)),
                new ShutdownTask(queueService)
        );
        Scheduler scheduler = new Scheduler(new SchedulerConfig(tasks));
        scheduler.init();
        return scheduler;
    }

    public void initQueues() {
        QueueService queueService = QueueService.getInstance();
        queueService.createNewQueue(POKEMON_SPECIES_URL_QUEUE);
        queueService.createNewQueue(POKEMON_SPRITES_QUEUE);
        queueService.createNewQueue(POKEMON_CRIES_QUEUE);
        queueService.createNewQueue(POKEMON_VARIETY_URL_QUEUE);
        queueService.createNewQueue(POKEMON_EVOLUTION_CHAIN_URL_QUEUE);
        queueService.createNewQueue(DATABASE_WAL_FLUSH_BUFFER_QUEUE);
    }

}
