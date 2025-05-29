package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCache;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiHost;
import com.github.mangila.pokedex.shared.tls.pool.TlsConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PokeApiMediaClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiMediaClient.class);

    private static PokeApiMediaClientConfig config;

    private final PokeApiHost host;
    private final TlsConnectionPool pool;
    private final TtlCache<String, byte[]> cache;

    private PokeApiMediaClient(PokeApiMediaClientConfig config) {
        this.host = config.pokeApiHost();
        this.pool = new TlsConnectionPool(config.tlsConnectionPoolConfig());
        pool.init();
        this.cache = new TtlCache<>(config.ttlCacheConfig());
    }

    public static void configure(PokeApiMediaClientConfig config) {
        Objects.requireNonNull(config, "PokeApiMediaClientConfig must not be null");
        if (PokeApiMediaClient.config != null) {
            throw new IllegalStateException("PokeApiMediaClientConfig is already configured");
        }
        log.info("Configuring PokeApiMediaClient with {}", config);
        PokeApiMediaClient.config = config;
    }

    private static final class Holder {
        private static final PokeApiMediaClient INSTANCE = new PokeApiMediaClient(config);
    }

    public static PokeApiMediaClient getInstance() {
        Objects.requireNonNull(config, "PokeApiMediaClient must be configured");
        return Holder.INSTANCE;
    }
}
