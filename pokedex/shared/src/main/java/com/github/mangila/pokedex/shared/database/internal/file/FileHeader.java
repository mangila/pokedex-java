package com.github.mangila.pokedex.shared.database.internal.file;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class FileHeader {

    public static final String POKEMON_MAGIC_NUMBER = "Pok3mon1";
    public static final byte[] POKEMON_MAGIC_NUMBER_BYTES = POKEMON_MAGIC_NUMBER.getBytes();
    public static final int VERSION = 1;

    // Header sizes
    public static final int POKEMON_MAGIC_NUMBER_SIZE = POKEMON_MAGIC_NUMBER.length();
    public static final int VERSION_SIZE = Integer.BYTES;
    public static final int RECORD_COUNT_SIZE = Integer.BYTES;
    public static final int INDEX_OFFSET_SIZE = Long.BYTES;
    public static final int DATA_OFFSET_SIZE = Long.BYTES;
    public static final int HEADER_SIZE = POKEMON_MAGIC_NUMBER_SIZE +
            VERSION_SIZE +
            RECORD_COUNT_SIZE +
            INDEX_OFFSET_SIZE +
            DATA_OFFSET_SIZE;

    // Header fixed positions
    public static final int RECORD_COUNT_POSITION = POKEMON_MAGIC_NUMBER_SIZE + VERSION_SIZE;
    public static final int INDEX_OFFSET_POSITION = RECORD_COUNT_POSITION + RECORD_COUNT_SIZE;
    public static final int DATA_OFFSET_POSITION = INDEX_OFFSET_POSITION + INDEX_OFFSET_SIZE;

    public static void readHeader(ByteBuffer buffer) {

    }

    public static void writeHeader(
            ByteBuffer buffer,
            int version,
            int recordCount,
            long indexOffset,
            long dataOffset
    ) {
        buffer.position(0);
        buffer.put(POKEMON_MAGIC_NUMBER_BYTES);
        buffer.putInt(version);
        buffer.putInt(recordCount);
        buffer.putLong(indexOffset);
        buffer.putLong(dataOffset);
        buffer.flip();
    }

    public static boolean isHeaderValid(ByteBuffer buffer) {
        var magic = new byte[POKEMON_MAGIC_NUMBER_SIZE];
        buffer.get(magic);
        return Arrays.equals(magic, POKEMON_MAGIC_NUMBER_BYTES);
    }

    public static int readVersion(ByteBuffer buffer) {
        return buffer.getInt(POKEMON_MAGIC_NUMBER_SIZE);
    }

    public static int readRecordCount(ByteBuffer buffer) {
        return buffer.getInt(RECORD_COUNT_POSITION);
    }

    public static long readIndexOffset(ByteBuffer buffer) {
        return buffer.getLong(INDEX_OFFSET_POSITION);
    }

    public static long readDataOffset(ByteBuffer buffer) {
        return buffer.getLong(DATA_OFFSET_POSITION);
    }

    public static void updateIndexOffset(ByteBuffer buffer, long newIndexOffset) {
        buffer.putLong(INDEX_OFFSET_POSITION, newIndexOffset);
    }

    public static void updateDataOffset(ByteBuffer buffer, long newDataOffset) {
        buffer.putLong(DATA_OFFSET_POSITION, newDataOffset);
    }

    public static void updateRecordCount(ByteBuffer buffer, int newRecordCount) {
        buffer.putInt(RECORD_COUNT_POSITION, newRecordCount);
    }
}
