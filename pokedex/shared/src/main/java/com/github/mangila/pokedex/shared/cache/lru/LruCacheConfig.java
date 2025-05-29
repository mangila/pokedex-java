package com.github.mangila.pokedex.shared.cache.lru;

public record LruCacheConfig(int capacity) {

    private static final int MAX_SIZE = 100;

    public LruCacheConfig {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Cache capacity must be greater than 0");
        }
        if (capacity > MAX_SIZE) {
            throw new IllegalArgumentException("Cache capacity must be less than or equal to " + MAX_SIZE);
        }
    }

}
