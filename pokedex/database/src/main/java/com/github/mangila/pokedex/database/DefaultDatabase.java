package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.database.model.WriteCallback;
import com.github.mangila.pokedex.database.serialization.DefaultSerializer;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.util.Ensure;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

public final class DefaultDatabase implements Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDatabase.class);
    private volatile boolean open = false;
    private final DatabaseConfig config;
    private final ScheduledExecutorService executorPool;
    private final DefaultSerializer serializer;
    private final Engine engine;

    public DefaultDatabase(DatabaseConfig config) {
        this.config = config;
        this.executorPool = VirtualThreadFactory.newScheduledThreadPool(1024);
        this.serializer = new DefaultSerializer();
        this.engine = new DefaultEngine(
                new FileManager(config),
                new Cache(new LruCache<>(config.lruCacheConfig()))
        );
    }

    @Override
    public synchronized void open() {
        if (isOpen()) {
            LOGGER.warn("Database already open");
            return;
        }
        open = true;
        engine.open();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public synchronized void close() {
        if (!isOpen()) {
            LOGGER.warn("Database already closed");
            return;
        }
        open = false;
        engine.close();
    }

    @Override
    public synchronized void flush() {
        if (!isOpen()) {
            LOGGER.warn("Database is closed");
            return;
        }
        engine.flush();
    }

    @Override
    public String getString(String key, String field) {
        Ensure.isTrue(isOpen(), "Database is closed");
        Key k = new Key(key);
        Field f = new Field(field);
        Value value = engine.get(k, f);
        return serializer.deserializeString(value.value());
    }

    @Override
    public CompletableFuture<String> getStringAsync(String key, String field) {
        return CompletableFuture.supplyAsync(() -> getString(key, field), executorPool);
    }

    @Override
    public WriteCallback put(String key, String field, byte[] value) {
        Ensure.isTrue(isOpen(), "Database is closed");
        Key k = new Key(key);
        Field f = new Field(field);
        Value v = new Value(value);
        return engine.put(k, f, v);
    }

    @Override
    public CompletableFuture<WriteCallback> putAsync(String key, String field, byte[] value) {
        return CompletableFuture.supplyAsync(() -> put(key, field, value), executorPool);
    }

    @Override
    public CompletableFuture<WriteCallback> putAsync(String key, String field, String value) {
        byte[] serializedValue = serializer.serialize(value);
        return putAsync(key, field, serializedValue);
    }

    @Override
    public CompletableFuture<WriteCallback> putAsync(String key, String field, Boolean value) {
        byte[] serializedValue = serializer.serialize(value);
        return putAsync(key, field, serializedValue);
    }

    @Override
    public CompletableFuture<WriteCallback> putAsync(String key, String field, BigInteger value) {
        byte[] serializedValue = serializer.serialize(value);
        return putAsync(key, field, serializedValue);
    }
}