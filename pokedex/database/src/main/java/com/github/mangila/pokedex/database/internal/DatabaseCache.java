package com.github.mangila.pokedex.database.internal;

import com.github.mangila.pokedex.database.internal.model.Key;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.cache.lru.LruCache;
import org.jspecify.annotations.Nullable;

public record DatabaseCache(LruCache<Key, Value> cache) {

    public @Nullable Value get(Key key) {
        return cache.get(key);
    }

    public void put(Key key, Value value) {
        cache.put(key, value);
    }

    public void truncate() {
        cache.truncate();
    }
}
