package com.github.mangila.pokedex.shared.database.internal.file.data;

import java.nio.ByteBuffer;
import java.util.zip.CRC32C;

public record DataRecord(int dataLength,
                         int version,
                         byte[] data,
                         long checksum) {

    public static DataRecord from(byte[] data, CRC32C crc32C, int version) {
        crc32C.reset();
        crc32C.update(data);
        return new DataRecord(data.length, version, data, crc32C.getValue());
    }

    public int getSize() {
        return Integer.BYTES + Integer.BYTES + data.length + Long.BYTES;
    }

    public void fill(ByteBuffer buffer) {
        buffer.putInt(dataLength);
        buffer.putInt(version);
        buffer.put(data);
        buffer.putLong(checksum);
    }
}
