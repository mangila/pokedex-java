package com.github.mangila.pokedex.database.internal;

import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.database.internal.io.internal.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.io.internal.model.WriteOperation;
import com.github.mangila.pokedex.database.internal.model.Key;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Engine {

    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private final DatabaseCache cache;
    private final DatabaseIo io;

    public Engine(DatabaseConfig config) {
        this.cache = new DatabaseCache(new LruCache<>(config.lruCacheConfig()));
        this.io = new DatabaseIo(config.databaseName());
    }

    public void init() throws IOException {
        io.init();
    }

    public void shutdown() {
        io.shutdown();
    }

    public void truncateCache() {
        cache.truncate();
    }

    public CompletableFuture<Boolean> truncateAsync() {
        truncateCache();
        return io.truncateAsync()
                .whenComplete((result, t) -> {
                    if (t != null) {
                        LOGGER.error("ERR", t);
                    } else if (result.equals(false)) {
                        LOGGER.warn("Failed to truncate database");
                    }
                });
    }

    public CompletableFuture<Boolean> deleteAsync() {
        truncateCache();
        return io.deleteAsync()
                .whenComplete((result, t) -> {
                    if (t != null) {
                        LOGGER.error("ERR", t);
                    } else if (result.equals(false)) {
                        LOGGER.warn("Failed to delete database");
                    }
                });
    }

    public CompletableFuture<@Nullable Value> getAsync(Key key) {
        Value value = cache.get(key);
        if (value != null) {
            return CompletableFuture.completedFuture(value);
        }
        return io.readAsync(new ReadOperation(key, new CompletableFuture<>()))
                .whenComplete((v, t) -> {
                    if (t != null) {
                        LOGGER.error("ERR", t);
                    }
                });
    }

    public CompletableFuture<Boolean> putAsync(Key key, Value value) {
        return io.writeAsync(new WriteOperation(key, value, WriteOperation.Operation.WRITE, new CompletableFuture<>()))
                .whenComplete((result, t) -> {
                    if (t != null) {
                        LOGGER.error("ERR", t);
                    } else if (result.equals(false)) {
                        LOGGER.warn("Failed to write to database");
                    } else {
                        cache.put(key, value);
                    }
                });
    }

    public int size() {
        return io.size();
    }
}
