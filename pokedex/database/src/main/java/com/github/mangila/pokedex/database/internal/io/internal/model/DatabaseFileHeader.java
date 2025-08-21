package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.shared.util.BufferUtils;
import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.ByteBuffer;

/**
 * File Header Section:
 * <pre>
 * +----------------+--------------------+----------------+
 * | Magic Number   | "yakvs" bytes     | File identifier|
 * | Version        | 4 bytes           | Format version |
 * | Record Count   | 4 bytes           | Num records    |
 * | Next Offset    | 8 bytes           | Write position |
 * +----------------+--------------------+----------------+
 * </pre>
 */
public record DatabaseFileHeader(
        MagicNumber magicNumber,
        Version version,
        RecordCount recordCount,
        Offset nextOffset
) {
    public DatabaseFileHeader {
        Ensure.notNull(magicNumber, MagicNumber.class);
        Ensure.notNull(version, Version.class);
        Ensure.notNull(recordCount, RecordCount.class);
        Ensure.notNull(nextOffset, Offset.class);
    }

    public record MagicNumber(byte[] value) {
        public static final String VALUE = "yakvs";
        public static final byte[] VALUE_BYTES = VALUE.getBytes();
        public static final int SIZE = VALUE_BYTES.length;
        public static final MagicNumber CURRENT = new MagicNumber(VALUE_BYTES);

        public MagicNumber {
            Ensure.equals(value, VALUE_BYTES);
            Ensure.equals(SIZE, VALUE_BYTES.length);
        }
    }

    public record Version(int value) {

        public static final int VALUE = 1;
        public static final int SIZE = Integer.BYTES;
        public static final Version CURRENT = new Version(VALUE);

        public Version {
            Ensure.equals(value, VALUE);
            Ensure.equals(Integer.BYTES, SIZE);
        }
    }

    public record RecordCount(int value) {
        public static final int SIZE = Integer.BYTES;
        public static final RecordCount ZERO = new RecordCount(0);

        public RecordCount {
            Ensure.min(0, value);
            Ensure.equals(SIZE, Integer.BYTES);
        }

        public static RecordCount increment(RecordCount recordCount) {
            int newCount = recordCount.value + 1;
            return new RecordCount(newCount);
        }
    }

    public static final OffsetBoundary HEADER_OFFSET_BOUNDARY = new OffsetBoundary(
            Offset.ZERO,
            new Offset(MagicNumber.SIZE + Version.SIZE + RecordCount.SIZE + Offset.SIZE)
    );

    public static DatabaseFileHeader EMPTY = new DatabaseFileHeader(
            MagicNumber.CURRENT,
            Version.CURRENT,
            RecordCount.ZERO,
            HEADER_OFFSET_BOUNDARY.end()
    );

    public static DatabaseFileHeader from(Buffer buffer) {
        byte[] magicNumber = buffer.getArray(MagicNumber.SIZE);
        Ensure.equals(magicNumber, MagicNumber.VALUE_BYTES);
        int version = buffer.getInt();
        int recordCount = buffer.getInt();
        long offset = buffer.getLong();
        return new DatabaseFileHeader(
                new MagicNumber(magicNumber),
                new Version(version),
                new RecordCount(recordCount),
                new Offset(offset)
        );
    }

    public Buffer toBuffer(boolean flip) {
        Offset headerEndOffset = HEADER_OFFSET_BOUNDARY.end();
        ByteBuffer buffer = BufferUtils.newByteBuffer((int) headerEndOffset.value());
        buffer.put(magicNumber.value);
        buffer.putInt(version.value);
        buffer.putInt(recordCount.value);
        buffer.putLong(nextOffset.value());
        if (flip) {
            buffer.flip();
        }
        return new Buffer(buffer);
    }
}
