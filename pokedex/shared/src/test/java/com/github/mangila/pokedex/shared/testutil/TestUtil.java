package com.github.mangila.pokedex.shared.testutil;

import com.github.mangila.pokedex.shared.cache.JsonResponseTtlCacheConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientConfig;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.tls.TlsConnectionPool;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;

import java.util.concurrent.TimeUnit;

public final class TestUtil {

    public static PokeApiClient createNewTestingPokeApiClient() throws InterruptedException {
        int maxConnections = 2;
        var pokeApiHost = PokeApiHost.fromDefault();
        var connectionPoolConfig = new TlsConnectionPoolConfig(
                pokeApiHost.host(),
                pokeApiHost.port(),
                new TlsConnectionPoolConfig.PoolConfig("pokedex-test-pool-1", maxConnections),
                new TlsConnectionPoolConfig.HealthCheckConfig(0, 10, TimeUnit.SECONDS)
        );
        return new PokeApiClient(new PokeApiClientConfig(
                pokeApiHost,
                connectionPoolConfig,
                JsonResponseTtlCacheConfig.fromDefaultConfig()));
    }

    public static TlsConnectionPool createNewTestingTlsConnectionPool(int maxConnections) {
        String host = "httpbin.org";
        int port = 443;
        return new TlsConnectionPool(
                new TlsConnectionPoolConfig(
                        host,
                        port,
                        new TlsConnectionPoolConfig.PoolConfig("pokedex-test-pool-2", maxConnections),
                        new TlsConnectionPoolConfig.HealthCheckConfig(
                                0,
                                10,
                                TimeUnit.SECONDS
                        )
                )
        );
    }

}
