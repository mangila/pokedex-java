package com.github.mangila.pokedex.database.internal.cache;

import com.github.mangila.pokedex.shared.cache.lru.LruCache;

public record DatabaseCache<V>(LruCache<String, V> cache) {
}
