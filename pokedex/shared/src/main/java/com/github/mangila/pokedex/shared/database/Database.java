package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.database.internal.DiskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class Database<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private final LruCache<String, V> cache;
    private final DiskHandler<V> disk;

    public Database(DatabaseConfig config) {
        this.cache = new LruCache<>(new LruCacheConfig(config.cacheCapacity()));
        this.disk = new DiskHandler<V>(config.databaseName());
        init();
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

    public void put(String key, V value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        disk.put(key, value);
        cache.put(key, value);
    }

    public V get(String key) {
        Objects.requireNonNull(key, "key must not be null");
        if (cache.hasKey(key)) {
            return cache.get(key);
        }
        var value = disk.get(key);
        if (value != null) {
            cache.put(key, value);
        }
        return value;
    }

    public void deleteFile() {
        try {
            disk.deleteFile();
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }
}
