package com.github.mangila.pokedex.shared.cache.lru;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LruCache<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LruCache.class);
    private final Map<K, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int capacity;
    private final CacheEntry head;
    private final CacheEntry tail;

    public boolean hasKey(K key) {
        return cache.containsKey(key);
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    private class CacheEntry {
        private final K key;
        private final V value;
        private CacheEntry next;
        private CacheEntry previous;

        private CacheEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public LruCache(LruCacheConfig config) {
        this.capacity = config.capacity();
        this.head = new CacheEntry(null, null);
        this.tail = new CacheEntry(null, null);
        head.next = tail;
        tail.previous = head;
    }

    public void put(K key, V value) {
        if (cache.containsKey(key)) {
            lastRecentlyUsed(cache.get(key));
            return;
        }
        if (cache.size() == capacity) {
            leastRecentlyUsed();
        }
        var entry = new CacheEntry(key, value);
        cache.put(key, entry);
        lastRecentlyUsed(entry);
    }

    public @Nullable V get(K key) {
        var entry = cache.get(key);
        if (entry == null) {
            LOGGER.debug("Cache miss for key {}", key);
            return null;
        }
        lastRecentlyUsed(entry);
        LOGGER.debug("Cache hit for key {}", key);
        return entry.value;
    }

    public void clear() {
        cache.clear();
        head.next = tail;
        tail.previous = head;
    }

    /**
     * 1. detach if it has links
     * 2. re-link to head
     */
    private void lastRecentlyUsed(CacheEntry entry) {
        if (entry.previous != null && entry.next != null) {
            entry.previous.next = entry.next;
            entry.next.previous = entry.previous;
        }

        entry.previous = head;
        entry.next = head.next;
        head.next.previous = entry;
        head.next = entry;
    }

    /**
     * 1. Remove from cache
     * 2. Create a new tail link
     */
    private void leastRecentlyUsed() {
        var entry = tail.previous;
        cache.remove(entry.key);
        entry.previous.next = entry.next;
        entry.next.previous = entry.previous;
    }
}
