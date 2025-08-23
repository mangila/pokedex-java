package com.github.mangila.pokedex.database.model;

import com.github.mangila.pokedex.shared.util.Ensure;

public record Value(byte[] value) {

    public static final Value EMPTY = new Value(new byte[0]);
    public static final short MAGIC_NUMBER = 3;

    public Value {
        Ensure.notNull(value, "value must not be null");
    }

    public int getBufferSize() {
        return Short.BYTES + Integer.BYTES + length();
    }

    public int length() {
        return value.length;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }
}
