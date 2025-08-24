package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCache;

public class Database {
    private final Engine engine;

    public Database(DatabaseConfig config) {
        this.engine = new Engine(
                new FileManager(new WalFileManager(config.databaseName())),
                new Cache(new LruCache<>(config.lruCacheConfig()))
        );
    }

    public Engine engine() {
        return engine;
    }
}
