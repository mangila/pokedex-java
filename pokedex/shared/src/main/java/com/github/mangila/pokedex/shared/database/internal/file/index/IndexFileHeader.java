package com.github.mangila.pokedex.shared.database.internal.file.index;

import java.nio.ByteBuffer;

public record IndexFileHeader(
        byte[] magicNumber,
        int version,
        int recordCount,
        long offset
) {

    public static final String MAGIC_NUMBER = "yakvsidx";
    public static final byte[] MAGIC_NUMBER_BYTES = MAGIC_NUMBER.getBytes();
    public static final int VERSION = 1;

    // Header sizes
    public static final int MAGIC_NUMBER_SIZE = MAGIC_NUMBER_BYTES.length;
    public static final int VERSION_SIZE = Integer.BYTES;
    public static final int RECORD_COUNT_SIZE = Integer.BYTES;
    public static final int OFFSET_SIZE = Long.BYTES;
    public static final int HEADER_SIZE = MAGIC_NUMBER_SIZE +
            VERSION_SIZE +
            RECORD_COUNT_SIZE +
            OFFSET_SIZE;

    public static IndexFileHeader defaultValue() {
        return new IndexFileHeader(MAGIC_NUMBER_BYTES, VERSION, 0, HEADER_SIZE);
    }

    public void fillAndFlip(ByteBuffer buffer) {
        buffer.put(magicNumber);
        buffer.putInt(version);
        buffer.putInt(recordCount);
        buffer.putLong(offset);
        buffer.flip();
    }

    public int incrementRecordCount() {
        return recordCount + 1;
    }
}
