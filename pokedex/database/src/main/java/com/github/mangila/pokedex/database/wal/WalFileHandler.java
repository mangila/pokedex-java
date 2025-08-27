package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.CallbackItemCollection;
import com.github.mangila.pokedex.database.model.Entry;
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

    void flush(FlushWriteBuffer flushWriteBuffer, CallbackItemCollection callbackItemCollection) throws IOException {
        int bufferLength = callbackItemCollection.bufferLength();
        if (bufferLength == 0) {
            return;
        }
        if (bufferLength <= flushWriteBuffer.bufferSize()) {
            Buffer buffer = flushWriteBuffer.get();
            for (Entry entry : callbackItemCollection.toValues()) {
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

    void replay() {
        try (Stream<Path> stream = Files.find(
                Paths.get("."),
                1, // search depth
                (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".wal")
        )) {
            List<Path> files = stream.toList();
            if (files.isEmpty()) {
                this.walFile = new WalFile(Paths.get(System.nanoTime() + ".wal"));
                return;
            }
            for (Path path : files) {
                LOGGER.info("Replaying {}", path);
                this.walFile = new WalFile(path);
                rotate(path.getFileName().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void rotate(String name) throws IOException {
        walFile.compress();
        this.walFile = new WalFile(Path.of(name));
    }

    public long size() {
        return walFile.size();
    }
}
