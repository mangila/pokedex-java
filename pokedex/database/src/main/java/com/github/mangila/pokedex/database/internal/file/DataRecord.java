package com.github.mangila.pokedex.database.internal.file;

import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.nio.ByteBuffer;

public record DataRecord(int dataLength, byte[] data) {

    public static DataRecord from(byte[] data) {
        return new DataRecord(data.length, data);
    }

    public int getSize() {
        return Integer.BYTES + data.length + Long.BYTES;
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = BufferUtils.newByteBuffer(getSize());
        fillAndFlip(buffer);
        return buffer;
    }

    private void fillAndFlip(ByteBuffer buffer) {
        buffer.putInt(dataLength);
        buffer.put(data);
        buffer.flip();
    }
}
