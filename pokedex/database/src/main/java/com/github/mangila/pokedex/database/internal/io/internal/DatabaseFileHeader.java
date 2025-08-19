package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.ByteBuffer;

import static com.github.mangila.pokedex.database.internal.io.internal.DatabaseFileHeader.MagicNumber.MAGIC_NUMBER_SIZE;

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
        Offset offset
) {
    public DatabaseFileHeader {
        Ensure.notNull(magicNumber, MagicNumber.class);
        Ensure.notNull(version, Version.class);
        Ensure.notNull(recordCount, RecordCount.class);
        Ensure.notNull(offset, Offset.class);
    }

    public record MagicNumber(byte[] value) {
        public static final String MAGIC_NUMBER = "yakvs";
        public static final byte[] MAGIC_NUMBER_BYTES = MAGIC_NUMBER.getBytes();
        public static final int MAGIC_NUMBER_SIZE = MAGIC_NUMBER_BYTES.length;
        public static final MagicNumber CURRENT_MAGIC_NUMBER = new MagicNumber(MAGIC_NUMBER_BYTES);

        public MagicNumber {
            Ensure.equals(value, MAGIC_NUMBER_BYTES);
            Ensure.equals(MAGIC_NUMBER_SIZE, MAGIC_NUMBER_BYTES.length);
        }
    }

    public record Version(int value) {
        public static final int VERSION = 1;
        public static final int VERSION_SIZE = Integer.BYTES;
        public static final Version CURRENT_VERSION = new Version(VERSION);

        public Version {
            Ensure.equals(value, VERSION);
            Ensure.equals(Integer.BYTES, VERSION_SIZE);
        }
    }

    public record RecordCount(int value) {
        public static final int RECORD_COUNT_SIZE = Integer.BYTES;

        public static final RecordCount ZERO = new RecordCount(0);

        public RecordCount {
            Ensure.min(0, value);
            Ensure.equals(RECORD_COUNT_SIZE, Integer.BYTES);
        }

        public static RecordCount increment(RecordCount recordCount) {
            int newCount = recordCount.value + 1;
            return new RecordCount(newCount);
        }
    }

    public static final Offset HEADER_SIZE = new Offset(
            MagicNumber.MAGIC_NUMBER_SIZE +
            Version.VERSION_SIZE +
            RecordCount.RECORD_COUNT_SIZE +
            Offset.OFFSET_SIZE
    );

    public static DatabaseFileHeader EMPTY = new DatabaseFileHeader(
            MagicNumber.CURRENT_MAGIC_NUMBER,
            Version.CURRENT_VERSION,
            RecordCount.ZERO,
            HEADER_SIZE
    );

    /**
     * Constructs a new instance of {@code DatabaseFileHeader} by deserializing the provided {@code ByteBuffer}.
     * The method reads the buffer sequentially to extract the magic number, version, record count, and offset.
     *
     * @param buffer the {@code ByteBuffer} containing the serialized representation of the {@code DatabaseFileHeader}.
     *               The buffer's position should be set to the start of the header data.
     * @return a {@code DatabaseFileHeader} instance constructed from the data read from the buffer.
     * @throws IllegalStateException    if the magic number in the buffer does not match the expected magic number.
     * @throws BufferUnderflowException if there is insufficient data in the buffer to read the expected fields.
     */
    public static DatabaseFileHeader from(ByteBuffer buffer) {
        byte[] magicNumber = new byte[MAGIC_NUMBER_SIZE];
        buffer.get(magicNumber);
        Ensure.equals(magicNumber, MagicNumber.MAGIC_NUMBER_BYTES);
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

    /**
     * Converts the current {@code DatabaseFileHeader} instance into a {@link ByteBuffer}.
     * The buffer includes the magic number, version, record count, and offset fields.
     * Optionally, the buffer can be flipped before being returned.
     *
     * @param flip if {@code true}, the buffer is flipped to prepare it for reading;
     *             if {@code false}, the buffer remains in its current state.
     * @return a {@link ByteBuffer} containing the serialized representation of the
     * {@code DatabaseFileHeader}.
     */
    public ByteBuffer toByteBuffer(boolean flip) {
        ByteBuffer buffer = BufferUtils.newByteBuffer((int) HEADER_SIZE.value());
        buffer.put(magicNumber.value);
        buffer.putInt(version.value);
        buffer.putInt(recordCount.value);
        buffer.putLong(offset.value());
        if (flip) {
            // Flip the buffer, set the position to zero
            buffer.flip();
        }
        return buffer;
    }
}
