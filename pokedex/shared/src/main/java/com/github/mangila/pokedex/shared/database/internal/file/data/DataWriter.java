package com.github.mangila.pokedex.shared.database.internal.file.data;

import com.github.mangila.pokedex.shared.database.internal.file.File;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32C;

public class DataWriter {

    private final File file;

    public DataWriter(File file) {
        this.file = file;
    }

    public long write(String key, byte[] data, long offset, CRC32C crc32C) throws IOException {
        var record = DataRecord.from(data, crc32C);
        int size = record.getSize();
        var buffer = file.getFileRegion(
                FileChannel.MapMode.READ_WRITE,
                offset,
                size
        );
        record.fill(buffer);
        buffer.force();
        return offset + size;
    }
}
