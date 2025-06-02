package com.github.mangila.pokedex.shared.database.internal.file;

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
}
