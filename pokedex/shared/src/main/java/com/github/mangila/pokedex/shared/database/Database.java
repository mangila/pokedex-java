package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.database.internal.DiskHandler;
import com.github.mangila.pokedex.shared.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Database<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private final LruCache<String, V> cache;
    private final DiskHandler disk;
    private final Supplier<V> instanceCreator;

    public Database(DatabaseConfig config,
                    Supplier<V> instanceCreator) {
        this.cache = new LruCache<>(config.lruCacheConfig());
        this.disk = new DiskHandler(config);
        this.instanceCreator = instanceCreator;
    }

    public void init() {
        log.info("Initializing database");
        try {
            disk.init();
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> putAsync(String key, V value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        try {
            var bytes = value.serialize();
            return disk.put(key, bytes)
                    .exceptionally(throwable -> {
                        log.error("ERR", throwable);
                        return Boolean.FALSE;
                    })
                    .whenComplete((ok, throwable) -> {
                        if (throwable != null) {
                            log.error("ERR", throwable);
                        }
                        if (Boolean.TRUE.equals(ok)) {
                            cache.put(key, value);
                        }
                    });
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Optional<V>> getAsync(String key) {
        Objects.requireNonNull(key, "key must not be null");
        if (cache.hasKey(key)) {
            return CompletableFuture.completedFuture(cache.get(key));
        }
        return disk.get(key)
                .exceptionally(throwable -> {
                    log.error("ERR", throwable);
                    return ArrayUtils.EMPTY_BYTE_ARRAY;
                })
                .thenApply(bytes -> {
                    if (ArrayUtils.isEmptyOrNull(bytes)) {
                        return Optional.empty();
                    }
                    try {
                        return Optional.ofNullable(instanceCreator.get()
                                .deserialize(bytes));
                    } catch (IOException e) {
                        log.error("ERR", e);
                        return Optional.empty();
                    }
                });
    }

    public boolean isEmpty() {
        return cache.isEmpty() && disk.isEmpty();
    }

    public void truncateDatabase() {
        try {
            cache.truncate();
            disk.truncateFiles();
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteDatabase() {
        try {
            cache.truncate();
            disk.deleteFiles();
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }
}
