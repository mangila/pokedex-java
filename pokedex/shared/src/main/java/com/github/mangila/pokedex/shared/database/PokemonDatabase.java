package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.cache.PokemonLruCache;
import com.github.mangila.pokedex.shared.cache.PokemonLruCacheConfig;
import com.github.mangila.pokedex.shared.database.internal.Engine;
import com.github.mangila.pokedex.shared.database.internal.Storage;
import com.github.mangila.pokedex.shared.model.Pokemon;

public class PokemonDatabase {

    private static PokemonDatabase instance;

    private final Engine engine;

    private PokemonDatabase() {
        this.engine = new Engine(
                new PokemonLruCache(new PokemonLruCacheConfig(10)),
                new Storage());
    }

    public static PokemonDatabase getInstance() {
        if (instance == null) {
            instance = new PokemonDatabase();
        }
        return instance;
    }

    public boolean put(String key, Pokemon value) {
        return engine.put(key, value);
    }

    public Pokemon get(String key) {
        return engine.get(key);
    }
}
