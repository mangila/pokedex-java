package com.github.mangila.pokedex.shared.database.internal.file.header;

import java.nio.MappedByteBuffer;
import java.util.Arrays;

public record FileHeader(
        byte[] magicNumber,
        int version,
        int recordCount,
        long offset
) {

    public static final String POKEMON_MAGIC_NUMBER = "Pok3mon1";
    public static final byte[] POKEMON_MAGIC_NUMBER_BYTES = POKEMON_MAGIC_NUMBER.getBytes();
    public static final int VERSION = 1;

    // Header sizes
    public static final int POKEMON_MAGIC_NUMBER_SIZE = POKEMON_MAGIC_NUMBER.length();
    public static final int VERSION_SIZE = Integer.BYTES;
    public static final int RECORD_COUNT_SIZE = Integer.BYTES;
    public static final int OFFSET_SIZE = Long.BYTES;
    public static final int HEADER_SIZE = POKEMON_MAGIC_NUMBER_SIZE +
            VERSION_SIZE +
            RECORD_COUNT_SIZE +
            OFFSET_SIZE;
    // Header fixed positions
    public static final int RECORD_COUNT_POSITION = POKEMON_MAGIC_NUMBER_SIZE + VERSION_SIZE;
    public static final int OFFSET_POSITION = RECORD_COUNT_POSITION + RECORD_COUNT_SIZE;

    public static FileHeader defaultValue() {
        return new FileHeader(POKEMON_MAGIC_NUMBER_BYTES, VERSION, 0, HEADER_SIZE);
    }

    public static void ensureValidMagicHeader(byte[] magic) {
        var equals = Arrays.equals(magic, POKEMON_MAGIC_NUMBER_BYTES);
        if (!equals) {
            throw new IllegalStateException("Invalid file header");
        }
    }

    public void fill(MappedByteBuffer buffer) {
        buffer.put(magicNumber);
        buffer.putInt(version);
        buffer.putInt(recordCount);
        buffer.putLong(offset);
    }

    public int incrementRecordCount() {
        return recordCount + 1;
    }
}
