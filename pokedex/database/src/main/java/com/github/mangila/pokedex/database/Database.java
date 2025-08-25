package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;

public class Database {

    private final DatabaseConfig config;
    private final Engine engine;

    public Database(DatabaseConfig config) {
        this.config = config;
        this.engine = new DefaultEngine(
                new FileManager(config),
                new Cache(new LruCache<>(config.lruCacheConfig()))
        );
    }

    public Engine engine() {
        return engine;
    }

    public DatabaseConfig config() {
        return config;
    }
}
