package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT THREAD SAFE
 */
class WalBufferPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalBufferPool.class);

    private static final int ONE_KB = 1024;
    private static final int TWO_KB = 2048;
    private static final int FOUR_KB = 4096;

    private final Buffer oneKbBuffer;
    private final Buffer twoKbBuffer;
    private final Buffer fourKbBuffer;

    WalBufferPool() {
        this.oneKbBuffer = Buffer.from(ONE_KB);
        this.twoKbBuffer = Buffer.from(TWO_KB);
        this.fourKbBuffer = Buffer.from(FOUR_KB);
    }

    @Nullable
    Buffer get(int size) {
        if (size <= ONE_KB) {
            return oneKbBuffer;
        } else if (size <= TWO_KB) {
            return twoKbBuffer;
        } else if (size <= FOUR_KB) {
            return fourKbBuffer;
        }
        LOGGER.warn("Buffer size too large: {}", size);
        return null;
    }


}
