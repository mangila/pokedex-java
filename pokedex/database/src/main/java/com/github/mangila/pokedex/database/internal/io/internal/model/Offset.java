package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record Offset(long value) {

    public static final int OFFSET_SIZE = Long.BYTES;

    public Offset {
        Ensure.min(0, value);
    }

}
