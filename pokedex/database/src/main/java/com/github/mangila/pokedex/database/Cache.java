package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;

public record Cache(LruCache<Key, Value> cache) {

    public Value getOrEmpty(Key key) {
        Value value = cache.get(key);
        if (value == null) {
            return Value.EMPTY;
        }
        return value;
    }

    public void put(Key key, Value value) {
        cache.put(key, value);
    }

    public void clear() {
        cache.clear();
    }
}
