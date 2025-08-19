package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.ByteBuffer;

public record Record(ByteBuffer value) {
    public Record {
        Ensure.notNull(value, ByteBuffer.class);
        Ensure.min(0, value.capacity());
    }
}
