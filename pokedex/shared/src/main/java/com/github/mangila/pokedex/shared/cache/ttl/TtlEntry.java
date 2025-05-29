package com.github.mangila.pokedex.shared.cache.ttl;

import java.time.Instant;
import java.util.Objects;

public record TtlEntry(
        Object value,
        Instant timestamp
) {

    public TtlEntry {
        Objects.requireNonNull(value);
        Objects.requireNonNull(timestamp);
    }
}
