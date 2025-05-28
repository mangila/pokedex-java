package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.cache.PokemonLruCache;
import com.github.mangila.pokedex.shared.model.Pokemon;

public class Engine {

    private final PokemonLruCache cache;
    private final DiskHandler disk;

    public Engine(PokemonLruCache cache,
                  DiskHandler disk) {
        this.cache = cache;
        this.disk = disk;
    }

    public Pokemon get(String key) {
        if (cache.hasKey(key)) {
            return cache.get(key);
        }
        var value = disk.get(key);
        if (value != null) {
            cache.put(key, value);
        }
        return value;
    }

    public void put(String key, Pokemon value) {
        disk.put(key, value);
        cache.put(key, value);
    }
}
