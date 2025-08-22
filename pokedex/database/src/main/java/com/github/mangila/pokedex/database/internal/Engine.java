package com.github.mangila.pokedex.database.internal;

import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.database.internal.io.internal.model.ReadOperation;
import com.github.mangila.pokedex.database.internal.io.internal.model.WriteOperation;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
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

    public void clearCache() {
        cache.clear();
    }

    public CompletableFuture<Boolean> truncateAsync() {
        clearCache();
        return io.truncateAsync()
                .whenComplete((result, t) -> {
                    if (t != null) {
                        LOGGER.error("ERR", t);
                    } else if (result.equals(false)) {
                        LOGGER.warn("Failed to truncate database");
                    } else {
                        LOGGER.info("Database truncated successfully");
                    }
                });
    }

    public CompletableFuture<Boolean> deleteAsync() {
        clearCache();
        return io.deleteAsync()
                .whenComplete((result, t) -> {
                    if (t != null) {
                        LOGGER.error("ERR", t);
                    } else if (result.equals(false)) {
                        LOGGER.warn("Failed to delete database");
                    } else {
                        LOGGER.info("Database deleted successfully");
                    }
                });
    }

    public CompletableFuture<Value> getAsync(Key key) {
        Value value = cache.get(key);
        if (value != null) {
            return CompletableFuture.completedFuture(value);
        }
        ReadOperation readOperation = new ReadOperation(key, new CompletableFuture<>());
        return io.readAsync(readOperation)
                .whenComplete((v, t) -> {
                    if (t != null) {
                        LOGGER.error("ERR", t);
                    } else if (Arrays.equals(v.value(), Value.EMPTY.value())) {
                        LOGGER.warn("Failed to read from database");
                    }
                });
    }

    public CompletableFuture<Boolean> putAsync(Key key, Value value) {
        WriteOperation writeOperation = new WriteOperation(
                key,
                value,
                WriteOperation.Operation.WRITE,
                new CompletableFuture<>()
        );
        return io.writeAsync(writeOperation)
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
