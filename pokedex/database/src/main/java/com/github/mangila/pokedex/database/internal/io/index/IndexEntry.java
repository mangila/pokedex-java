package com.github.mangila.pokedex.database.internal.io.index;

import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.model.Key;
import com.github.mangila.pokedex.shared.util.BufferUtils;

import java.nio.ByteBuffer;

/**
 * <h3>Index File Entries</h3>
 * <pre>
 * +---------------+------------+--------------------------------+
 * | Field         | Size       | Description                    |
 * +---------------+------------+--------------------------------+
 * | Key Length    | 4 bytes    | Length of key bytes            |
 * | Key Bytes     | Variable   | Key data                       |
 * | Data Offset   | 8 bytes    | Points to record in data file  |
 * +---------------+------------+--------------------------------+
 */
public record IndexEntry(Key key, Offset offset) {

    public int getSize() {
        return Integer.BYTES + key.length() + Long.BYTES;
    }

    public ByteBuffer toByteBuffer(boolean flip) {
        ByteBuffer buffer = BufferUtils.newByteBuffer(getSize());
        buffer.putInt(key.length());
        buffer.put(key.value().getBytes());
        buffer.putLong(offset.value());
        if (flip) {
            // Flip the buffer, set the position to zero
            buffer.flip();
        }
        return buffer;
    }
}
