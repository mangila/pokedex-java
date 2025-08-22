package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record Value(byte[] value) {

    public static final Value EMPTY = new Value(new byte[0]);

    public Value {
        Ensure.notNull(value, "value must not be null");
    }

    public int length() {
        return value.length;
    }
}
