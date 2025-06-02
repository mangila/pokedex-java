package com.github.mangila.pokedex.shared.database.internal.file.data;

import com.github.mangila.pokedex.shared.database.internal.file.File;

import java.io.IOException;

public class DataReader {

    private final File file;

    public DataReader(File file) {
        this.file = file;
    }

    public DataRecord read(long offset) throws IOException {
        var lengthBuffer = file.readAndFlip(offset, Integer.BYTES);
        var length = lengthBuffer.getInt();
        var dataBuffer = file.readAndFlip(offset + Integer.BYTES, length);
        var data = dataBuffer.array();
        var checksumBuffer = file.readAndFlip(offset + Integer.BYTES + length, Long.BYTES);
        var checksum = checksumBuffer.getLong();
        return new DataRecord(data.length, data, checksum);
    }
}
