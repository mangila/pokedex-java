package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.nio.ByteBuffer;
import java.util.zip.CRC32C;

public record DataRecord(int dataLength,
                         byte[] data,
                         long checksum) {

    public static DataRecord from(byte[] data, CRC32C crc32C) {
        crc32C.reset();
        crc32C.update(data);
        return new DataRecord(data.length, data, crc32C.getValue());
    }

    public static DataRecord from(byte[] data, long checksum) {
        return new DataRecord(data.length, data, checksum);
    }

    public int getSize() {
        return Integer.BYTES + data.length + Long.BYTES;
    }

    public void fillAndFlip(ByteBuffer buffer) {
        buffer.putInt(dataLength);
        buffer.put(data);
        buffer.putLong(checksum);
        buffer.flip();
    }

    public ByteBuffer toByteBuffer() {
        var buffer = BufferUtils.newByteBuffer(getSize());
        fillAndFlip(buffer);
        return buffer;
    }
}
