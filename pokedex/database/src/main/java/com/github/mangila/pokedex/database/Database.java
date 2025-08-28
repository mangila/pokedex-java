package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.model.WriteCallback;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;

import java.util.concurrent.CompletableFuture;

public class Database {

    private final DatabaseConfig config;
    private final Engine engine;

    public Database(DatabaseConfig config) {
        this.config = config;
        this.engine = new DefaultEngine(
                new FileManager(config),
                new Cache(new LruCache<>(config.lruCacheConfig()))
        );
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            flush();
            close();
        }));
    }

    private void flush() {
        engine.flush();
    }

    public boolean isOpen() {
        return engine.isOpen();
    }

    public void open() {
        engine.open();
    }

    public void close() {
        engine.close();
    }

    public CompletableFuture<WriteCallback> putAsync(String key, String field, byte[] value) {
        return engine.putAsync(key, field, value);
    }

    public DatabaseConfig config() {
        return config;
    }
}
