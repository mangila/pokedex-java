package com.github.mangila.pokedex.shared.cache.ttl;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record TtlCacheConfig(
        Duration ttl,
        EvictionConfig evictionConfig
) {

    public static TtlCacheConfig fromDefaultConfig() {
        return new TtlCacheConfig(Duration.ofMinutes(5), new EvictionConfig(10, 30, TimeUnit.SECONDS));
    }

    public record EvictionConfig(long initialDelay, long delay, TimeUnit timeUnit) {

        public EvictionConfig {
            if (initialDelay < 0) {
                throw new IllegalArgumentException("initialDelay must be greater than or equal to 0");
            }
            if (delay < 0) {
                throw new IllegalArgumentException("delay must be greater than or equal to 0");
            }
            Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        }

    }

    public TtlCacheConfig {
        Objects.requireNonNull(ttl, "ttl must not be null");
        Objects.requireNonNull(evictionConfig, "evictionConfig must not be null");
    }
}
