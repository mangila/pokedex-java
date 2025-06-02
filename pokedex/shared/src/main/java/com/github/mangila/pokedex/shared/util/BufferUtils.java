package com.github.mangila.pokedex.shared.util;

import java.nio.ByteBuffer;

public final class BufferUtils {

    public static ByteBuffer newByteBuffer(int capacity) {
        return ByteBuffer.allocate(capacity);
    }
}
