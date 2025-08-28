package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlushWriteBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlushWriteBuffer.class);
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private final int bufferSize;
    private final Buffer heapBuffer;
    private final Buffer directBuffer;

    FlushWriteBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.heapBuffer = Buffer.from(bufferSize);
        this.directBuffer = Buffer.fromDirect(bufferSize);
    }

    FlushWriteBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    Buffer getHeap() {
        heapBuffer.clear();
        return heapBuffer;
    }

    Buffer getDirect() {
        directBuffer.clear();
        return directBuffer;
    }

    int bufferSize() {
        return bufferSize;
    }
}
