package com.github.mangila.pokedex.shared.database.internal.file.data;

import com.github.mangila.pokedex.shared.database.internal.file.File;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class DataReader {

    private final File file;

    public DataReader(File file) {
        this.file = file;
    }

    public DataRecord read(long offset) throws IOException {
        var buffer = file.getFileRegion(
                FileChannel.MapMode.READ_ONLY,
                offset,
                Integer.BYTES
        );
        var length = buffer.getInt();
        buffer = file.getFileRegion(
                FileChannel.MapMode.READ_ONLY,
                offset + Integer.BYTES,
                length
        );
        var data = new byte[length];
        buffer.get(data);
        buffer = file.getFileRegion(
                FileChannel.MapMode.READ_ONLY,
                offset + Integer.BYTES + length,
                Long.BYTES
        );
        var checksum = buffer.getLong();
        return new DataRecord(data.length, data, checksum);
    }
}
