package com.github.mangila.pokedex.database.model;

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

    public int capacity() {
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
        return getArray(capacity());
    }

    public void flip() {
        value.flip();
    }

    public void clear() {
        value.clear();
    }

    public void putInt(int i) {
        value.putInt(i);
    }

    public void put(byte[] bytes) {
        value.put(bytes);
    }

    public void put(Value value) {
        putInt(value.length());
        put(value.value());
    }

    public void put(Key key) {
        putInt(key.length());
        put(key.getBytes());
    }
}
