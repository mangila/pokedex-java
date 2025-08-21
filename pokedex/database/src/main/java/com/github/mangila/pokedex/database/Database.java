package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.internal.Engine;
import com.github.mangila.pokedex.database.internal.model.Key;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Database<T extends DatabaseObject<T>> implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private final Engine engine;
    private final Supplier<T> instanceCreator;

    public Database(DatabaseConfig config,
                    Supplier<T> instanceCreator) {
        this.engine = new Engine(config);
        this.instanceCreator = instanceCreator;
    }

    public void init() {
        try {
            LOGGER.info("Initializing database");
            engine.init();
        } catch (IOException e) {
            LOGGER.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        LOGGER.info("Closing database");
        engine.shutdown();
    }

    public CompletableFuture<Boolean> truncateAsync() {
        return engine.truncateAsync();
    }

    public CompletableFuture<Boolean> deleteAsync() {
        return engine.deleteAsync();
    }

    public int size() {
        return engine.size();
    }

    public CompletableFuture<Boolean> putAsync(String key, T value) {
        Ensure.notNull(key, "key must not be null");
        Ensure.notBlank(key, "key must not be blank");
        Ensure.notNull(value, "value must not be null");
        try {
            Key k = new Key(key);
            Value v = new Value(value.serialize());
            return engine.putAsync(k, v);
        } catch (Exception e) {
            LOGGER.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<@Nullable T> getAsync(String key) {
        Ensure.notNull(key, "key must not be null");
        Ensure.notBlank(key, "key must not be blank");
        Key k = new Key(key);
        return engine.getAsync(k)
                .handle((value, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("ERR", throwable);
                        return null;
                    }
                    if (value == null) {
                        return null;
                    }
                    try {
                        return instanceCreator.get()
                                .deserialize(value.value());
                    } catch (IOException e) {
                        LOGGER.error("ERR", e);
                        return null;
                    }
                });
    }
}
