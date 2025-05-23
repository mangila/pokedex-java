package com.github.mangila.pokedex.shared.cache;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record JsonResponseTtlCacheConfig(
        Duration ttl,
        long initialDelay,
        long delay,
        TimeUnit timeUnit
) {

    public static JsonResponseTtlCacheConfig fromDefaultConfig() {
        return new JsonResponseTtlCacheConfig(Duration.ofMinutes(5), 10, 30, TimeUnit.SECONDS);
    }

    public JsonResponseTtlCacheConfig {
        if (initialDelay < 0) {
            throw new IllegalArgumentException("initialDelay must be greater than or equal to 0");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be greater than or equal to 0");
        }
        Objects.requireNonNull(ttl, "ttl must not be null");
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
    }
}
