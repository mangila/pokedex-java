package com.github.mangila.pokedex.database.internal.file;

import com.github.mangila.pokedex.shared.util.BufferUtils;
import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.ByteBuffer;

public record FileHeader(
        byte[] magicNumber,
        int version,
        int recordCount,
        long offset
) {

    public static final String MAGIC_NUMBER = "yakvs";
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

    public static FileHeader from(ByteBuffer buffer) {
        byte[] magicNumber = new byte[MAGIC_NUMBER_SIZE];
        buffer.get(magicNumber);
        Ensure.equals(magicNumber, MAGIC_NUMBER_BYTES);
        int version = buffer.getInt();
        int recordCount = buffer.getInt();
        long offset = buffer.getLong();
        return new FileHeader(magicNumber, version, recordCount, offset);
    }

    public static FileHeader defaultValue() {
        return new FileHeader(MAGIC_NUMBER_BYTES, VERSION, 0, HEADER_SIZE);
    }

    public int incrementRecordCount() {
        return recordCount + 1;
    }

    public FileHeader updateOffset(long newOffset) {
        return new FileHeader(magicNumber, version, incrementRecordCount(), newOffset);
    }

    public ByteBuffer toByteBuffer() {
        var buffer = BufferUtils.newByteBuffer(HEADER_SIZE);
        fillAndFlip(buffer);
        return buffer;
    }

    public void fillAndFlip(ByteBuffer buffer) {
        buffer.put(magicNumber);
        buffer.putInt(version);
        buffer.putInt(recordCount);
        buffer.putLong(offset);
        buffer.flip();
    }
}
