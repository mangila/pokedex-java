package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.cache.LruCache;

public class Engine {

    private final LruCache cache;
    private final Storage storage;

    public Engine(LruCache cache,
                  Storage storage) {
        this.cache = cache;
        this.storage = storage;
    }

    public byte[] get(String key) {
        return null;
    }

    public boolean put(String key, byte[] value) {
        return false;
    }
}
