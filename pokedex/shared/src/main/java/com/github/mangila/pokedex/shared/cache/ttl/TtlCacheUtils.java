package com.github.mangila.pokedex.shared.cache.ttl;

import java.time.Duration;
import java.time.Instant;

public final class TtlCacheUtils {

    private TtlCacheUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isExpired(TtlEntry entry, Duration ttl) {
        return entry.timestamp()
                .plusMillis(ttl.toMillis())
                .isBefore(Instant.now());
    }

}
