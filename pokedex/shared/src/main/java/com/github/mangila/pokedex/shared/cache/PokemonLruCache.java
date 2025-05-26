package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PokemonLruCache {

    private static final int MAX_SIZE = 100;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int capacity;
    private final CacheEntry head;
    private final CacheEntry tail;

    private static class CacheEntry {
        private final String key;
        private final Pokemon value;
        private CacheEntry next;
        private CacheEntry previous;

        private CacheEntry(String key, Pokemon value) {
            this.key = key;
            this.value = value;
        }
    }

    public PokemonLruCache(PokemonLruCacheConfig config) {
        this.capacity = config.capacity();
        this.head = new CacheEntry(null, null);
        this.tail = new CacheEntry(null, null);
        head.next = tail;
        tail.previous = head;
    }

    public void put(String key, Pokemon value) {
        if (cache.containsKey(key)) {
            moveToHead(cache.get(key));
            return;
        }
        if (cache.size() == capacity) {
            removeOldest();
        }
        var entry = new CacheEntry(key, value);
        cache.put(key, entry);
        moveToHead(entry);
    }

    public Pokemon get(String key) {
        var entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        moveToHead(entry);
        return entry.value;
    }

    private void moveToHead(CacheEntry entry) {
        if (entry.previous != null && entry.next != null) {
            entry.previous.next = entry.next;
            entry.next.previous = entry.previous;
        }

        entry.previous = head;
        entry.next = head.next;
        head.next.previous = entry;
        head.next = entry;
    }

    private void removeOldest() {
        var entry = tail.previous;
        cache.remove(entry.key);
        entry.previous.next = entry.next;
        entry.next.previous = entry.previous;
    }
}
