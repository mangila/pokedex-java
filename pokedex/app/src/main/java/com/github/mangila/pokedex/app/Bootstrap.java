package com.github.mangila.pokedex.app;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.Scheduler;
import com.github.mangila.pokedex.scheduler.SchedulerConfig;
import com.github.mangila.pokedex.scheduler.task.*;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.util.List;

import static com.github.mangila.pokedex.shared.Config.*;

public final class Bootstrap {

    public void configurePokemonDatabase() {
        PokemonDatabase.configureDefaultSettings();
    }

    public void configurePokeApiClient() {
        PokeApiClient.configureDefaultSettings();
    }

    public void initQueues() {
        QueueService queueService = QueueService.getInstance();
        queueService.createNewBlockingQueue(POKEMON_SPECIES_URL_QUEUE);
        queueService.createNewBlockingQueue(POKEMON_SPRITES_QUEUE);
        queueService.createNewBlockingQueue(POKEMON_CRIES_QUEUE);
        queueService.createNewBlockingQueue(POKEMON_VARIETY_URL_QUEUE);
        queueService.createNewBlockingQueue(POKEMON_EVOLUTION_CHAIN_URL_QUEUE);
        queueService.createNewBlockingQueue(DATABASE_WAL_FLUSH_BUFFER_QUEUE);
        queueService.createNewBlockingQueue(DATABASE_WAL_COMPRESSION_QUEUE);
    }

    public Scheduler initScheduler() {
        QueueService queueService = QueueService.getInstance();
        PokeApiClient pokeApiClient = PokeApiClient.getInstance();
        PokemonDatabase pokemonDatabase = PokemonDatabase.getInstance();
        pokemonDatabase.instance().open();
        TaskExecutor taskExecutor = new TaskExecutor(
                VirtualThreadFactory.newScheduledThreadPool(256)
        );
        List<Task> tasks = List.of(
                new InsertCriesTask(pokeApiClient, queueService.getBlockingQueue(POKEMON_CRIES_QUEUE), pokemonDatabase),
                new InsertEvolutionChainResponse(queueService.getBlockingQueue(POKEMON_EVOLUTION_CHAIN_URL_QUEUE), pokemonDatabase),
                new InsertSpeciesResponseTask(pokeApiClient, queueService.getBlockingQueue(POKEMON_SPECIES_URL_QUEUE), pokemonDatabase),
                new InsertSpritesTask(pokeApiClient, queueService.getBlockingQueue(POKEMON_SPRITES_QUEUE), pokemonDatabase),
                new InsertVarietyResponseTask(queueService.getBlockingQueue(POKEMON_VARIETY_URL_QUEUE), pokemonDatabase),
                new QueuePokemonsTask(pokeApiClient, queueService.getBlockingQueue(POKEMON_SPECIES_URL_QUEUE), POKEMON_LIMIT),
                new ShutdownTask()
        );
        Scheduler scheduler = new Scheduler(new SchedulerConfig(tasks, taskExecutor));
        scheduler.init();
        return scheduler;
    }

}
