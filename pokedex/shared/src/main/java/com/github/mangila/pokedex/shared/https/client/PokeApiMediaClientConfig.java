package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiHost;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;

public record PokeApiMediaClientConfig(
        PokeApiHost pokeApiHost,
        TlsConnectionPoolConfig tlsConnectionPoolConfig,
        TtlCacheConfig ttlCacheConfig
) {
}