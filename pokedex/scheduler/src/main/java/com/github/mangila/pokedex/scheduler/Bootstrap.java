package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.task.*;
import com.github.mangila.pokedex.shared.PokemonDatabase;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiMediaClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiMediaClientConfig;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.json.JsonParserConfig;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiHost;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.github.mangila.pokedex.scheduler.SchedulerApplication.*;

public class Bootstrap {

    public void initQueues() {
        var queueService = QueueService.getInstance();
        queueService.createNewQueue(POKEMON_SPECIES_URL_QUEUE);
        queueService.createNewQueue(POKEMON_SPECIES_URL_DL_QUEUE);
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
        PokemonDatabase.configure(new DatabaseConfig(
                new DatabaseName("pokedex"),
                new LruCacheConfig(10),
                new DatabaseConfig.CompactThreadConfig(10, 5, TimeUnit.SECONDS),
                new DatabaseConfig.ReaderThreadConfig(3, 50),
                new DatabaseConfig.WriteThreadConfig(10)));
        PokemonDatabase.getInstance()
                .get()
                .init();
    }

    public void configureScheduler() {
        var pokeApiClient = PokeApiClient.getInstance();
        var queueService = QueueService.getInstance();
        var pokemonDatabase = PokemonDatabase.getInstance();
        var map = new HashMap<String, Task>();
        var insertCriesTask = new InsertCriesTask(pokeApiClient, queueService);
        map.put(insertCriesTask.name(), insertCriesTask);
        var insertPokemonTask = new InsertPokemonTask(pokeApiClient, queueService, pokemonDatabase);
        map.put(insertPokemonTask.name(), insertPokemonTask);
        var insertSpritesTask = new InsertSpritesTask(pokeApiClient, queueService);
        map.put(insertSpritesTask.name(), insertSpritesTask);
        var queuePokemonsTask = new QueuePokemonsTask(pokeApiClient, queueService, 5);
        map.put(queuePokemonsTask.name(), queuePokemonsTask);
        var shutdownTask = new ShutdownTask(queueService);
        map.put(shutdownTask.name(), shutdownTask);
        Scheduler.configure(new SchedulerConfig(map));
        Scheduler.getInstance().init();
    }
}
