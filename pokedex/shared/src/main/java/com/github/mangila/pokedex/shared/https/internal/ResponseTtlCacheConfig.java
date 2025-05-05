package com.github.mangila.pokedex.shared.https.internal;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record ResponseTtlCacheConfig(
        Duration ttl,
        long initialDelay,
        long delay,
        TimeUnit timeUnit
) {
    public ResponseTtlCacheConfig {
        Objects.requireNonNull(ttl, "ttl must not be null");
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
    }
}
