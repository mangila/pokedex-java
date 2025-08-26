package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.EntryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class WalFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandler.class);
    private final WalWriteBuffer walWriteBuffer;
    private WalFile walFile;

    WalFileHandler(WalWriteBuffer walWriteBuffer) {
        this.walWriteBuffer = walWriteBuffer;
        try {
            this.walFile = new WalFile(Path.of("hej.wal"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void flush(EntryCollection entryCollection) throws IOException {
        int bufferLength = entryCollection.bufferLength();
        if (bufferLength == 0) {
            return;
        }
        write(bufferLength, entryCollection.toValues());
    }

    private void write(int len, List<Entry> entries) throws IOException {
        if (len <= walWriteBuffer.bufferSize()) {
            LOGGER.info("Writing {} bytes", len);
            Buffer buffer = walWriteBuffer.get();
            for (Entry entry : entries) {
                entry.fill(buffer);
            }
            buffer.flip();
            try {
                walFile.write(buffer);
            } finally {
                buffer.clear();
            }
        } else {
            // todo: write to disk in chunks
            throw new IllegalStateException("Buffer size exceeded %d".formatted(len));
        }
    }

    void open() {

    }

    void replay() {

    }

    void rotate() {

    }
}
