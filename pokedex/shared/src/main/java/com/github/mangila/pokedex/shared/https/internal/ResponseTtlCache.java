package com.github.mangila.pokedex.shared.https.internal;

import com.github.mangila.pokedex.shared.https.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ResponseTtlCache {

    private static final Logger log = LoggerFactory.getLogger(ResponseTtlCache.class);
    private static final Map<String, TtlCacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(Thread
            .ofVirtual()
            .factory());

    private final Duration ttl;

    public ResponseTtlCache(Duration ttl) {
        this.ttl = ttl;
        SCHEDULER.scheduleWithFixedDelay(this::evict, 10, 10, TimeUnit.SECONDS);
    }

    private record TtlCacheEntry(Response value,
                                 Instant timestamp) {
        TtlCacheEntry {
            Objects.requireNonNull(value);
            Objects.requireNonNull(timestamp);
        }

    }

    public void put(String key, Response value) {
        CACHE.put(key, new TtlCacheEntry(value, Instant.now()));
    }

    public Response get(String key) {
        var entry = CACHE.get(key);
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

    private boolean isExpired(TtlCacheEntry entry) {
        return entry.timestamp
                .plusMillis(ttl.toMillis())
                .isBefore(Instant.now());
    }

    private void evict() {
        log.debug("Evicting expired cache entries");
        CACHE.entrySet()
                .removeIf(entry -> {
                    boolean isExpired = isExpired(entry.getValue());
                    if (isExpired) {
                        log.debug("Evicting entry: {}", entry.getKey());
                    }
                    return isExpired;
                });
    }
}
