package com.github.mangila.pokedex.database.engine;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;

public record Cache(LruCache<Field, Value> cache) {
    Value getOrEmpty(Field field) {
        Value value = cache.get(field);
        if (value == null) {
            return Value.EMPTY;
        }
        return value;
    }

    void put(Field field, Value value) {
        cache.put(field, value);
    }

    void clear() {
        cache.clear();
    }
}
