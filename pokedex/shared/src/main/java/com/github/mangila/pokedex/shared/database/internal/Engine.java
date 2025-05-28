package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.cache.PokemonLruCache;
import com.github.mangila.pokedex.shared.model.Pokemon;

public class Engine {

    private final PokemonLruCache cache;
    private final Storage storage;

    public Engine(PokemonLruCache cache,
                  Storage storage) {
        this.cache = cache;
        this.storage = storage;
    }

    public Pokemon get(String key) {
        if (cache.hasKey(key)) {
            return cache.get(key);
        }
        var value = storage.get(key);
        if (value != null) {
            cache.put(key, value);
        }
        return value;
    }

    public void put(String key, Pokemon value) {
        storage.put(key, value);
        cache.put(key, value);
    }
}
