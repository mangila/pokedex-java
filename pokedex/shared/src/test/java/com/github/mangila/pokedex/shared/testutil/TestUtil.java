package com.github.mangila.pokedex.shared.testutil;

import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.tls.TlsConnectionPool;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;

import java.util.concurrent.TimeUnit;

public final class TestUtil {

    private static final PokeApiHost POKE_API_HOST = PokeApiHost.fromDefault();

    public static PokeApiClient createNewTestingPokeApiClient() throws InterruptedException {
        int maxConnections = 2;
        return new PokeApiClient(
                POKE_API_HOST,
                new TlsConnectionPoolConfig(
                        POKE_API_HOST.host(),
                        POKE_API_HOST.port(),
                        new TlsConnectionPoolConfig.PoolConfig("pokedex-test-pool", maxConnections),
                        new TlsConnectionPoolConfig.HealthCheckConfig(
                                0,
                                10,
                                TimeUnit.SECONDS
                        )
                )
        );
    }

    public static TlsConnectionPool createNewTestingTlsConnectionPool() {
        String host = "httpbin.org";
        int port = 443;
        int maxConnections = 2;
        return new TlsConnectionPool(
                new TlsConnectionPoolConfig(
                        host,
                        port,
                        new TlsConnectionPoolConfig.PoolConfig("pokedex-test-pool", maxConnections),
                        new TlsConnectionPoolConfig.HealthCheckConfig(
                                0,
                                10,
                                TimeUnit.SECONDS
                        )
                )
        );
    }

}
