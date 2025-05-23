package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.cache.JsonResponseTtlCacheConfig;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;

public record PokeApiClientConfig(
        PokeApiHost pokeApiHost,
        TlsConnectionPoolConfig tlsConnectionPoolConfig,
        JsonResponseTtlCacheConfig jsonResponseTtlCacheConfig
) {
}
