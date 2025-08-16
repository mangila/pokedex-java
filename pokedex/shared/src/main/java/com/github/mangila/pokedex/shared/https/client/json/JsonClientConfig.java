package com.github.mangila.pokedex.shared.https.client.json;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;
import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.tls.pool.TlsConnectionPoolConfig;

public record JsonClientConfig(
        String host,
        JsonParser jsonParser,
        TlsConnectionPoolConfig poolConfig,
        TtlCacheConfig ttlCacheConfig
) {
}
