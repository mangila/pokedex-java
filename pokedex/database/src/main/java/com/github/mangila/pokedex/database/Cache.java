package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.HashKey;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;

public record Cache(LruCache<HashKey, Value> cache) {

    public Value getOrEmpty(HashKey hashKey) {
        Value value = cache.get(hashKey);
        if (value == null) {
            return Value.EMPTY;
        }
        return value;
    }

    public void put(HashKey hashKey, Value value) {
        cache.put(hashKey, value);
    }

    public void clear() {
        cache.clear();
    }
}
