package com.github.mangila.pokedex.shared.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public final class BufferUtils {

    public static ByteBuffer newByteBuffer(int capacity) {
        return ByteBuffer.allocate(capacity);
    }

    public static ByteBuffer newByteBufferDirect(int capacity) {
        return ByteBuffer.allocateDirect(capacity);
    }

    public static ByteArrayOutputStream newByteArrayOutputStream() {
        return new ByteArrayOutputStream();
    }

    public static ByteArrayOutputStream newByteArrayOutputStream(int size) {
        return new ByteArrayOutputStream(size);
    }

    public static ByteArrayInputStream newByteArrayInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }
}
