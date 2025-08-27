package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT THREAD SAFE
 */
class FlushWriteBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlushWriteBuffer.class);
    private static final Buffer EMPTY_BUFFER = Buffer.from(0);
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private final int bufferSize;
    private final Buffer buffer;

    FlushWriteBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = Buffer.from(bufferSize);
    }

    FlushWriteBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    Buffer get(int len) {
        if (len == 0) {
            LOGGER.warn("Size is 0 will return Empty buffer");
            return EMPTY_BUFFER;
        }
        len = nextPowerOfTwo(len);
        if (len <= bufferSize) {
            return buffer;
        } else {
            LOGGER.debug("Length is over {}, value needs to be chunked", bufferSize);
            return EMPTY_BUFFER;
        }
    }

    Buffer get() {
        return buffer;
    }

    int bufferSize() {
        return bufferSize;
    }


    private static int nextPowerOfTwo(int n) {
        Ensure.min(0, n);
        int highest = Integer.highestOneBit(n);
        return (n == highest) ? n : highest * 2;
    }
}
