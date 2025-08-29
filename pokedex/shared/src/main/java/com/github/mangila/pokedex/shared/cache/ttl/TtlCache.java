package com.github.mangila.pokedex.shared.cache.ttl;

import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;


public class TtlCache<K, V> {

    private static final Logger log = LoggerFactory.getLogger(TtlCache.class);
    private final Map<K, TtlEntry> cache = new ConcurrentHashMap<>();
    private final TtlCacheConfig config;

    private final ScheduledExecutorService executor;

    public TtlCache(TtlCacheConfig config) {
        this.config = config;
        this.executor = VirtualThreadFactory.newSingleThreadScheduledExecutor();
        scheduleTtlEvictionThread();
    }

    public void put(K key, V value) {
        cache.put(key, new TtlEntry(value, Instant.now()));
    }

    @SuppressWarnings("unchecked")
    public @Nullable V get(K key) {
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

    public void clear() {
        cache.clear();
    }

    public void shutdown() {
        executor.shutdown();
    }

    private void scheduleTtlEvictionThread() {
        executor.scheduleWithFixedDelay(() -> {
                    log.debug("Running eviction thread");
                    cache.entrySet()
                            .removeIf(entry -> {
                                boolean isExpired = TtlCacheUtils.isExpired(entry.getValue(), config.ttl());
                                if (isExpired) {
                                    log.debug("Evicting entry: {}", entry.getKey());
                                }
                                return isExpired;
                            });
                },
                config.evictionConfig().initialDelay(),
                config.evictionConfig().delay(),
                config.evictionConfig().timeUnit());
    }
}