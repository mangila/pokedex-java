package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.shared.util.BufferUtils;
import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.ByteBuffer;

public record Buffer(ByteBuffer value) {
    public Buffer {
        Ensure.notNull(value, ByteBuffer.class);
        Ensure.min(0, value.capacity());
    }

    public static Buffer from(int capacity) {
        return new Buffer(BufferUtils.newByteBuffer(capacity));
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

    public byte[] getArray(int length) {
        byte[] array = new byte[length];
        value.get(array);
        return array;
    }

    public byte[] getArray() {
        return getArray(length());
    }

    public void flip() {
        value.flip();
    }

    public void clear() {
        value.clear();
    }
}
