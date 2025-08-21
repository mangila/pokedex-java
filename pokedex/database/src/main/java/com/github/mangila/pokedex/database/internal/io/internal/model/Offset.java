package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record Offset(long value) {
    public static final int SIZE = Long.BYTES;
    public static final Offset ZERO = new Offset(0);

    public Offset {
        Ensure.min(0, value);
    }

}
