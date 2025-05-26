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
        return storage.get(key);
    }

    public boolean put(String key, Pokemon value) {
        return false;
    }
}
