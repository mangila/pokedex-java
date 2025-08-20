package com.github.mangila.pokedex.database.internal.io.internal.model;

import java.nio.ByteBuffer;

public record DataEntry(byte[] data) {

    public int getSize() {
        return Integer.BYTES + data.length;
    }

    public ByteBuffer toByteBuffer(boolean flip) {
        var buffer = ByteBuffer.allocate(getSize());
        buffer.putInt(data.length);
        buffer.put(data);
        if (flip) {
            // Flip the buffer, set the position to zero
            buffer.flip();
        }
        return buffer;
    }

}
