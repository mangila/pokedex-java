package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.EntryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

class WalFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandler.class);
    private final WalBufferPool walBufferPool;
    private WalFile walFile;

    WalFileHandler(WalBufferPool walBufferPool) {
        this.walBufferPool = walBufferPool;
        try {
            this.walFile = new WalFile(Path.of("hej.wal"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void flush(FlushOperation flushOperation) throws IOException {
        EntryCollection entries = flushOperation.entries();
        if (flushOperation.reason() == FlushOperation.Reason.THRESHOLD_BIG_WRITE) {
            // TODO chunk big writes
        } else {
            Buffer buffer = walBufferPool.get(entries.bufferSize());
            if (buffer == null) {
                // TODO chunk big writes
                buffer = Buffer.from(entries.bufferSize());
            }
            for (Entry entry : entries.collection()) {
                entry.fill(buffer);
            }
            buffer.flip();
            try {
                walFile.write(buffer);
            } finally {
                buffer.clear();
            }
        }
    }

    void open() {

    }

    void replay() {

    }

    void rotate() {

    }
}
