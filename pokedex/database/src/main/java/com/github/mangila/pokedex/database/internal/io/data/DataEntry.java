package com.github.mangila.pokedex.database.internal.io.data;

import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.nio.ByteBuffer;

/**
 * Data Records Section:
 * <pre>
 * +-----------------+----------------------+
 * | Record Length   | 4 bytes             |
 * | Data            | Variable length     |
 * +-----------------+----------------------+
 * </pre>
 *
 */
public record DataEntry(byte[] data) {

    public int getSize() {
        return Integer.BYTES + data.length;
    }

    public ByteBuffer toByteBuffer(boolean flip) {
        ByteBuffer buffer = BufferUtils.newByteBuffer(getSize());
        buffer.putInt(data.length);
        buffer.put(data);
        if (flip) {
            // Flip the buffer, set the position to zero
            buffer.flip();
        }
        return buffer;
    }

}
