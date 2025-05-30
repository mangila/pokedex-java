package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.database.internal.DiskHandler;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PokemonDatabase {

    private static final Logger log = LoggerFactory.getLogger(PokemonDatabase.class);

    private static PokemonDatabaseConfig config;

    private final LruCache<String, Pokemon> cache;
    private final DiskHandler disk;

    private PokemonDatabase(PokemonDatabaseConfig config) {
        this.cache = new LruCache<>(new LruCacheConfig(config.cacheCapacity()));
        this.disk = new DiskHandler(config.pokemonFileName());
    }

    public static void configure(PokemonDatabaseConfig config) {
        Objects.requireNonNull(config, "PokemonDatabaseConfig must not be null");
        if (PokemonDatabase.config != null) {
            throw new IllegalStateException("PokemonDatabaseConfig is already configured");
        }
        log.info("Configuring PokemonDatabase with {}", config);
        PokemonDatabase.config = config;
    }

    private static final class Holder {
        private static final PokemonDatabase INSTANCE = new PokemonDatabase(config);
    }

    public static PokemonDatabase getInstance() {
        Objects.requireNonNull(config, "PokemonDatabase must be configured");
        return Holder.INSTANCE;
    }

    public void put(String key, Pokemon value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        cache.put(key, value);
        disk.put(key, value);
    }

    public Pokemon get(String key) {
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
        disk.deleteFile();
    }
}
