package com.github.mangila.pokedex.shared.testutil;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientConfig;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiHost;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
import com.github.mangila.pokedex.shared.tls.pool.TlsConnectionPool;

import java.util.concurrent.TimeUnit;

public final class TestUtil {

    public static PokeApiClient createNewTestingPokeApiClient() {
        var pokeApiHost = PokeApiHost.fromDefault();
        var connectionPoolConfig = new TlsConnectionPoolConfig(
                pokeApiHost.host(),
                pokeApiHost.port(),
                2,
                new TlsConnectionPoolConfig.HealthCheckConfig(0, 10, TimeUnit.SECONDS)
        );
        var config = new PokeApiClientConfig(
                pokeApiHost,
                connectionPoolConfig,
                TtlCacheConfig.fromDefaultConfig());
        PokeApiClient.configure(config);
        return PokeApiClient.getInstance();
    }

    public static TlsConnectionPool createNewTestingTlsConnectionPool(int maxConnections) {
        String host = "httpbin.org";
        int port = 443;
        return new TlsConnectionPool(
                new TlsConnectionPoolConfig(
                        host,
                        port,
                        maxConnections,
                        new TlsConnectionPoolConfig.HealthCheckConfig(
                                0,
                                10,
                                TimeUnit.SECONDS
                        )
                )
        );
    }

}
