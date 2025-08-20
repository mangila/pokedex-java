package com.github.mangila.pokedex.database.internal.model;

import com.github.mangila.pokedex.shared.util.ArrayUtils;
import com.github.mangila.pokedex.shared.util.Ensure;

public record Value(byte[] value) {

    public static final Value EMPTY = new Value(ArrayUtils.EMPTY_BYTE_ARRAY);

    public Value {
        Ensure.notNull(value, "value must not be null");
    }
}
