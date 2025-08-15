package com.github.mangila.pokedex.shared.https.client.json;

import com.github.mangila.pokedex.shared.cache.ttl.TtlCacheConfig;

public record JsonClientConfig(
        String host,
        TtlCacheConfig ttlCacheConfig
) {
}
