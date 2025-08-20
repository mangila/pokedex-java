package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.ByteBuffer;

public record Entry(ByteBuffer value) {
    public Entry {
        Ensure.notNull(value, ByteBuffer.class);
        Ensure.min(0, value.capacity());
    }

    public int length() {
        return value.capacity();
    }

    public int getInt() {
        return value.getInt();
    }

    public long getLong() {
        return value.getLong();
    }
    public byte[] getArray() {
        byte[] array = new byte[length()];
        value.get(array);
        return array;
    }
}
