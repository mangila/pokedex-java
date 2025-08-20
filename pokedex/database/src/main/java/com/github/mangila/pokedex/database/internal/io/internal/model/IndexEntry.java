package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.database.internal.io.model.Key;
import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.nio.ByteBuffer;

public record IndexEntry(Key key, Offset offset) {

    public int getSize() {
        return Integer.BYTES + key.length() + Long.BYTES;
    }
    public ByteBuffer toByteBuffer(boolean flip) {
        ByteBuffer buffer = BufferUtils.newByteBuffer(getSize());
        buffer.putInt(key.length());
        buffer.put(key.value().getBytes());
        buffer.putLong(offset.value());
        if (flip) {
            // Flip the buffer, set the position to zero
            buffer.flip();
        }
        return buffer;
    }
}
