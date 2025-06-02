package com.github.mangila.pokedex.shared.database.internal.file.data;

import com.github.mangila.pokedex.shared.database.internal.file.File;
import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.io.IOException;
import java.util.zip.CRC32C;

public class DataWriter {

    private final File file;

    public DataWriter(File file) {
        this.file = file;
    }

    public long write(byte[] data, long offset, CRC32C crc32C) throws IOException {
        var record = DataRecord.from(data, crc32C);
        int size = record.getSize();
        var buffer = BufferUtils.newByteBuffer(size);
        record.fillAndFlip(buffer);
        file.write(buffer, offset);
        return offset + size;
    }
}
