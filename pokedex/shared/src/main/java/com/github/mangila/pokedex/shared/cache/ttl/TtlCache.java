package com.github.mangila.pokedex.shared.cache.ttl;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TtlCache<K, V> {

    private static final Logger log = LoggerFactory.getLogger(TtlCache.class);

    private final Map<K, TtlEntry> cache = new ConcurrentHashMap<>();
    private final TtlCacheConfig config;

    public TtlCache(TtlCacheConfig config) {
        this.config = config;
        EvictionThread<K> evictionThread = new EvictionThread<>(cache, config.ttl());
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(evictionThread, config.initialDelay(),
                        config.delay(),
                        config.timeUnit());
    }

    public void put(K key, V value) {
        cache.put(key, new TtlEntry(value, Instant.now()));
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        var entry = cache.get(key);
        if (entry == null) {
            log.debug("Cache miss for key {}", key);
            return null;
        }
        if (TtlCacheUtils.isExpired(entry, config.ttl())) {
            log.debug("Cache entry {} is expired", key);
            return null;
        }
        log.debug("Cache hit for key {}", key);
        return (V) entry.value();
    }

    public boolean hasKey(K key) {
        return cache.containsKey(key);
    }
}