package com.github.mangila.pokedex.shared.cache.lru;

import com.github.mangila.pokedex.shared.util.Ensure;

public record LruCacheConfig(int capacity) {

    public LruCacheConfig {
        Ensure.min(1, capacity);
        Ensure.max(100, capacity);
    }

}
