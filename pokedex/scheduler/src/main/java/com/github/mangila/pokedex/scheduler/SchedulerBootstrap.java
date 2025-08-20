package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.client.PokeApiClientConfig;
import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.https.client.json.JsonClientConfig;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.tls.pool.TlsConnectionPoolConfig;

import static com.github.mangila.pokedex.scheduler.SchedulerApplication.*;

public class SchedulerBootstrap {

    public void configureQueues() {
        QueueService queueService = QueueService.getInstance();
        queueService.createNewQueue(POKEMON_SPECIES_URL_QUEUE);
        queueService.createNewQueue(POKEMON_SPECIES_URL_DL_QUEUE);
        queueService.createNewQueue(POKEMON_SPRITES_QUEUE);
        queueService.createNewQueue(POKEMON_CRIES_QUEUE);
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
}
