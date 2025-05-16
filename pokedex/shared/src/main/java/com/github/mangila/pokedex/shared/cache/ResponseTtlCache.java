package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class ResponseTtlCache {

    private static final Logger log = LoggerFactory.getLogger(ResponseTtlCache.class);

    private final Map<String, TtlCacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService evictionThread = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    private final ResponseTtlCacheConfig config;

    public ResponseTtlCache(ResponseTtlCacheConfig config) {
        this.config = config;
    }

    private record TtlCacheEntry(JsonResponse value,
                                 Instant timestamp) {
        TtlCacheEntry {
            Objects.requireNonNull(value);
            Objects.requireNonNull(timestamp);
        }
    }

    public void put(String key, JsonResponse value) {
        cache.put(key, new TtlCacheEntry(value, Instant.now()));
    }

    public JsonResponse get(String key) {
        var entry = cache.get(key);
        if (entry == null) {
            log.debug("Cache miss for key {}", key);
            return null;
        }
        if (isExpired(entry)) {
            log.debug("Cache entry {} is expired", key);
            return null;
        }
        log.debug("Cache hit for key {}", key);
        return entry.value;
    }

    public boolean hasKey(String key) {
        return cache.containsKey(key);
    }

    public void clear() {
        cache.clear();
        log.debug("Cache cleared");
    }

    public void startEvictionThread() {
        log.debug("Starting eviction thread");
        evictionThread.scheduleWithFixedDelay(this::evict,
                config.initialDelay(),
                config.delay(),
                config.timeUnit());
    }

    public void shutdownEvictionThread() {
        evictionThread.shutdown();
        log.debug("Eviction thread shutdown");
    }

    public boolean isShutdownAndTerminated() {
        return evictionThread.isShutdown() && evictionThread.isTerminated();
    }

    public void evict() {
        log.debug("Evicting expired cache entries");
        cache.entrySet()
                .removeIf(entry -> {
                    boolean isExpired = isExpired(entry.getValue());
                    if (isExpired) {
                        log.debug("Evicting entry: {}", entry.getKey());
                    }
                    return isExpired;
                });
    }

    private boolean isExpired(TtlCacheEntry entry) {
        return entry.timestamp
                .plusMillis(config.ttl().toMillis())
                .isBefore(Instant.now());
    }
}
