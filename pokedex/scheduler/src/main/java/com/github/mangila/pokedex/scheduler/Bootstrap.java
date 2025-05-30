package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.*;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.database.PokemonDatabase;
import com.github.mangila.pokedex.shared.database.PokemonDatabaseConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiMediaClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiMediaClientConfig;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.JsonParserConfig;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiHost;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.mangila.pokedex.scheduler.SchedulerApplication.*;

public class Bootstrap {

    public void initQueues() {
        var queueService = QueueService.getInstance();
        queueService.createNewQueue(POKEMON_SPECIES_URL_QUEUE);
        queueService.createNewQueue(POKEMON_SPRITES_QUEUE);
        queueService.createNewQueue(POKEMON_CRIES_QUEUE);
    }

    public void configureJsonParser() {
        JsonParser.configure(new JsonParserConfig(64));
    }

    public void configurePokeApiClient() {
        var pokeApiHost = PokeApiHost.fromDefault();
        var connectionPoolConfig = new TlsConnectionPoolConfig(
                pokeApiHost.host(),
                pokeApiHost.port(),
                5,
                new TlsConnectionPoolConfig.HealthCheckConfig(10, 10, TimeUnit.SECONDS)
        );
        var config = new PokeApiClientConfig(
                pokeApiHost,
                connectionPoolConfig,
                TtlCacheConfig.fromDefaultConfig()
        );
        PokeApiClient.configure(config);
    }

    public void configurePokeApiMediaClient() {
        var pokeApiHost = PokeApiHost.fromDefault();
        var connectionPoolConfig = new TlsConnectionPoolConfig(
                pokeApiHost.host(),
                pokeApiHost.port(),
                5,
                new TlsConnectionPoolConfig.HealthCheckConfig(10, 10, TimeUnit.SECONDS)
        );
        var config = new PokeApiMediaClientConfig(
                pokeApiHost,
                connectionPoolConfig,
                TtlCacheConfig.fromDefaultConfig()
        );
        PokeApiMediaClient.configure(config);
    }

    public void configurePokemonDatabase() {
        PokemonDatabase.configure(
                new PokemonDatabaseConfig(
                        "pokedex.pokemon",
                        10)
        );
    }

    public void configureScheduler() {
        var pokeApiClient = PokeApiClient.getInstance();
        var queueService = QueueService.getInstance();
        var pokemonDatabase = PokemonDatabase.getInstance();
        List<Task> tasks = new ArrayList<>();
        tasks.add(new InsertCriesTask(pokeApiClient, queueService));
        tasks.add(new InsertPokemonTask(pokeApiClient, queueService, pokemonDatabase));
        tasks.add(new InsertSpritesTask(pokeApiClient, queueService));
        tasks.add(new QueuePokemonsTask(pokeApiClient, queueService, 1025));
        tasks.add(new ShutdownTask(queueService));
        Scheduler.configure(new SchedulerConfig(tasks));
    }

    public void initScheduler() {
        Scheduler.getInstance().init();
    }
}
