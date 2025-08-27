package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.EntryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

class WalFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandler.class);
    private WalFile walFile;

    WalFileHandler(WalFile walFile) {
        this.walFile = walFile;
    }

    WalFileHandler() {

    }

    void flush(FlushWriteBuffer flushWriteBuffer, EntryCollection entryCollection) throws IOException {
        int bufferLength = entryCollection.bufferLength();
        if (bufferLength == 0) {
            return;
        }
        if (bufferLength <= flushWriteBuffer.bufferSize()) {
            Buffer buffer = flushWriteBuffer.get();
            for (Entry entry : entryCollection.toValues()) {
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
            throw new IllegalStateException("Buffer size exceeded %d".formatted(bufferLength));
        }
    }


    void open() {

    }

    WalFile replay() {
        try (Stream<Path> stream = Files.find(
                Paths.get("."),
                1, // search depth
                (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".txt")
        )) {
            List<Path> files = stream.toList();
            for (Path path : files) {
                LOGGER.info("Replaying {}", path);
                rotate(path.getFileName().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.walFile = new WalFile(Paths.get(System.nanoTime() + ".wal"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return walFile;
    }

    void rotate(String name) throws IOException {
        walFile.compress();
        this.walFile = new WalFile(Path.of(name));
    }

    public long size() {
        return walFile.size();
    }
}
