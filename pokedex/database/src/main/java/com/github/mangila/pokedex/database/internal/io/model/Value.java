package com.github.mangila.pokedex.database.internal.io.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record Value(byte[] value) {
    public Value {
        Ensure.notNull(value, "value must not be null");
    }
}
