package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.database.internal.DiskHandler;
import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.Objects;

public class PokemonDatabase {

    private static PokemonDatabase instance;

    private final LruCache<String, Pokemon> cache;
    private final DiskHandler disk;

    private PokemonDatabase(PokemonDatabaseConfig config) {
        this.cache = new LruCache<>(new LruCacheConfig(config.cacheCapacity()));
        this.disk = new DiskHandler(config.fileName());
    }

    public static PokemonDatabase getInstance() {
        if (instance == null) {
            instance = new PokemonDatabase(
                    new PokemonDatabaseConfig(
                            "db.pokemon",
                            10)
            );
        }
        return instance;
    }

    public void put(String key, Pokemon value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        cache.put(key, value);
        disk.put(key, value);
    }

    public Pokemon get(String key) {
        Objects.requireNonNull(key);
        if (cache.hasKey(key)) {
            return cache.get(key);
        }
        var value = disk.get(key);
        if (value != null) {
            cache.put(key, value);
        }
        return value;
    }
}
