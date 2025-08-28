package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.CallbackItemCollection;
import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.shared.Config;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
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

    /**
     * Fill the heap buffer for quick fills,
     * write with the direct buffer for quick IO writes
     * and sync the WAL file after every write.
     * If the item collection has a size greater than the buffer size,
     * does a transaction write to skip partial write in the WAL file
     *
     * @throws IOException
     */
    void flush(FlushWriteBuffer flushWriteBuffer, CallbackItemCollection callbackItemCollection) throws IOException {
        int bufferLength = callbackItemCollection.bufferLength();
        if (bufferLength == 0) {
            return;
        }
        if (bufferLength <= flushWriteBuffer.bufferSize()) {
            Buffer heap = flushWriteBuffer.getHeap();
            Buffer direct = flushWriteBuffer.getDirect();
            for (Entry entry : callbackItemCollection.toValues()) {
                entry.fill(heap);
            }
            heap.flip();
            direct.put(heap);
            direct.flip();
            walFile.write(direct);
            walFile.sync();
        } else {
            // TODO: write to disk in chunks, create transaction file
            // TODO: clean up orphaned transaction files in case of failure
            throw new IllegalStateException("Buffer size exceeded %d".formatted(bufferLength));
        }
    }

    void replay() {
        try (Stream<Path> stream = Files.find(
                Paths.get("."),
                1,
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
                rotate(Path.of(System.nanoTime() + ".wal"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void rotate(Path newFileName) throws IOException {
        close();
        Path source = path();
        QueueService.getInstance()
                .add(Config.DATABASE_WAL_COMPRESSION_QUEUE, new QueueEntry(source));
        walFile = new WalFile(newFileName);
    }

    public long size() {
        return walFile.size();
    }

    public void close() throws IOException {
        walFile.close();
    }

    public Path path() {
        return walFile.path();
    }
}
