package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.client.PokeApiClientConfig;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.https.client.json.JsonClientConfig;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.tls.pool.TlsConnectionPoolConfig;

import java.util.concurrent.TimeUnit;

import static com.github.mangila.pokedex.scheduler.SchedulerApplication.*;

public class Bootstrap {

    public QueueService initQueueService() {
        QueueService queueService = new QueueService();
        queueService.createNewQueue(POKEMON_SPECIES_URL_QUEUE);
        queueService.createNewQueue(POKEMON_SPECIES_URL_DL_QUEUE);
        queueService.createNewQueue(POKEMON_SPRITES_QUEUE);
        queueService.createNewQueue(POKEMON_CRIES_QUEUE);
        return queueService;
    }

    public PokeApiClient initPokeApiClient() {
        return new PokeApiClient(
                new PokeApiClientConfig(
                        new JsonClientConfig(
                                POKEAPI_HOST,
                                JsonParser.DEFAULT,
                                new TlsConnectionPoolConfig(POKEAPI_HOST, POKEAPI_PORT, 5),
                                TtlCacheConfig.defaultConfig()
                        )
                )
        );
    }

    public PokemonDatabase initPokemonDatabase() {
        return PokemonDatabase.init(
                new DatabaseConfig(
                        new DatabaseName("pokedex"),
                        new LruCacheConfig(50),
                        new DatabaseConfig.CompactThreadConfig(10, 10, TimeUnit.MINUTES),
                        new DatabaseConfig.ReaderThreadConfig(3, 100),
                        new DatabaseConfig.WriteThreadConfig(100)
                )
        );
    }
}
