package com.github.mangila.pokedex.shared.cache.ttl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

public record EvictionThread<K>(Map<K, TtlEntry> cache, Duration ttl) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(EvictionThread.class);

    @Override
    public void run() {
        log.debug("Running eviction thread");
        cache.entrySet()
                .removeIf(entry -> {
                    boolean isExpired = TtlCacheUtils.isExpired(entry.getValue(), ttl);
                    if (isExpired) {
                        log.debug("Evicting entry: {}", entry.getKey());
                    }
                    return isExpired;
                });
    }
}
