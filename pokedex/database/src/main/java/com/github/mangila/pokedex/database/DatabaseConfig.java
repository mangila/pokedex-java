package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.util.Ensure;

public record DatabaseConfig(
        DatabaseName databaseName,
        LruCacheConfig lruCacheConfig) {
    public DatabaseConfig {
        Ensure.notNull(databaseName, "database name must not be null");
        Ensure.notNull(lruCacheConfig, "lru cache config must not be null");
    }
}
