package com.github.mangila.pokedex.database.internal.file;

import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.nio.ByteBuffer;

public record IndexEntry(int keyLength, byte[] key, long dataOffset) {

    public static IndexEntry from(byte[] key, long dataOffset) {
        return new IndexEntry(key.length, key, dataOffset);
    }

    public int getSize() {
        return Integer.BYTES + key.length + Long.BYTES;
    }

    public void fillAndFlip(ByteBuffer buffer) {
        buffer.putInt(keyLength);
        buffer.put(key);
        buffer.putLong(dataOffset);
        buffer.flip();
    }

    public ByteBuffer toByteBuffer() {
        var buffer = BufferUtils.newByteBuffer(getSize());
        fillAndFlip(buffer);
        return buffer;
    }
}
