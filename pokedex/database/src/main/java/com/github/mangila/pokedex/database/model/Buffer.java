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

    public static Buffer from(ByteBuffer buffer) {
        return new Buffer(buffer);
    }

    public static Buffer fromDirect(int capacity) {
        return new Buffer(BufferUtils.newByteBufferDirect(capacity));
    }

    public int capacity() {
        return value.capacity();
    }

    public int remaining() {
        return value.remaining();
    }

    public boolean isEmpty() {
        return remaining() == 0;
    }

    public short getShort() {
        return value.getShort();
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

    public void putShort(short s) {
        value.putShort(s);
    }

    public void put(byte[] bytes) {
        value.put(bytes);
    }

    public void put(Key key) {
        putShort(Key.MAGIC_NUMBER);
        putInt(key.length());
        put(key.getBytes());
    }

    public byte get() {
        return value.get();
    }

    public byte[] get(Key key) {
        int magic = getShort();
        Ensure.isTrue(magic == Key.MAGIC_NUMBER, "Not valid Key magic number");
        int len = getInt();
        Ensure.isTrue(len == key.length(), "Key length does not match");
        return getArray(len);
    }

    public void put(Field field) {
        putShort(Field.MAGIC_NUMBER);
        putInt(field.length());
        put(field.getBytes());
    }

    public byte[] get(Field field) {
        int magic = getShort();
        Ensure.isTrue(magic == Field.MAGIC_NUMBER, "Not valid Field magic number");
        int len = getInt();
        Ensure.isTrue(len == field.length(), "Field length does not match");
        return getArray(len);
    }

    public void put(Value value) {
        putShort(Value.MAGIC_NUMBER);
        putInt(value.length());
        put(value.value());
    }

    public byte[] getValue() {
        int magic = getShort();
        Ensure.isTrue(magic == Value.MAGIC_NUMBER, "Not valid Value magic number");
        int len = getInt();
        Ensure.isTrue(len == value.remaining(), "Value length does not match");
        return getArray(len);
    }

    public void put(Buffer b) {
        value.put(b.value);
    }
}
