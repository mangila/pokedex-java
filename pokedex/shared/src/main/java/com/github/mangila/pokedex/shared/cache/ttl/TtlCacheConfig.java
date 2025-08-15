package com.github.mangila.pokedex.shared.cache.ttl;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public record TtlCacheConfig(
        Duration ttl,
        EvictionConfig evictionConfig
) {

    public static TtlCacheConfig defaultConfig() {
        return new TtlCacheConfig(Duration.ofMinutes(5), new EvictionConfig(10, 30, TimeUnit.SECONDS));
    }

    public TtlCacheConfig {
        Ensure.notNull(ttl, "ttl must not be null");
        Ensure.notNull(evictionConfig, "evictionConfig must not be null");
    }

    public record EvictionConfig(long initialDelay, long delay, TimeUnit timeUnit) {

        public EvictionConfig {
            Ensure.min(1, initialDelay);
            Ensure.min(1, delay);
            Ensure.notNull(timeUnit, "timeUnit must not be null");
        }

    }
}
