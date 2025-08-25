package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.config.WalConfig;
import com.github.mangila.pokedex.database.model.Buffer;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

class WalFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandler.class);
    private final WalConfig walConfig;
    private final WalFile walFile;
    private final WalTable walTable;
    private final ExecutorService flushExecutor;
    private final CompletableFuture<Boolean> flushCallback;
    private final CountDownLatch flushLatch;

    WalFileHandler(WalFile walFile, WalConfig walConfig) {
        this.walFile = walFile;
        this.walConfig = walConfig;
        this.walTable = new WalTable(new ConcurrentSkipListMap<>(Comparator.comparing(Key::value)));
        this.flushExecutor = VirtualThreadFactory.newSingleThreadExecutor();
        this.flushCallback = new CompletableFuture<>();
        this.flushLatch = new CountDownLatch(1);
        flushExecutor.submit(new WalFlushThread(this));
    }

    CompletableFuture<WalIoOperationStatus> appendAsync(Key key, Field field, Value value) {
        var walAppendFuture = new CompletableFuture<WalIoOperationStatus>();
        int bufferSize = key.getBufferSize() + field.getBufferSize() + value.getBufferSize();
        Buffer writeBuffer = Buffer.from(bufferSize);
        writeBuffer.put(key);
        writeBuffer.put(field);
        writeBuffer.put(value);
        writeBuffer.flip();
        walFile.write(writeBuffer, walAppendFuture);
        return walAppendFuture
                .thenApply(status -> {
                    if (status == WalIoOperationStatus.SUCCESS) {
                        walTable.put(key, field, value);
                    } else if (status == WalIoOperationStatus.FAILED) {
                        LOGGER.warn("Failed to write to WAL file");
                    }
                    if (walFile.size() >= walConfig.flushSizeThreshold() && shouldFlush()) {
                        flushLatch.countDown();
                    }
                    return status;
                });
    }

    boolean isFlushing() {
        return walFile.status().get() == WalFileStatus.FLUSHING;
    }

    CompletableFuture<Boolean> flushCallback() {
        return flushCallback;
    }

    WalFile walFile() {
        return walFile;
    }

    WalTable walTable() {
        return walTable;
    }

    CountDownLatch flushLatch() {
        return flushLatch;
    }

    boolean shouldFlush() {
        return walFile.status().compareAndSet(WalFileStatus.OPEN, WalFileStatus.FLUSHING);
    }

    void closeAndDelete(Duration duration) throws IOException {
        VirtualThreadFactory.terminateGracefully(flushExecutor, duration);
        walFile.close();
        walFile.delete();
    }

    CompletableFuture<Boolean> flush() throws InterruptedException {
        CompletableFuture<Boolean> flushFuture = new CompletableFuture<>();
        if (walFile.awaitInFlightWritesWithRetry(Duration.ofMinutes(1), 3)) {
            LOGGER.info("Flushing WAL file {}", walFile.getPath());
            // TODO: snapshot waltable and send to disk via future or smt
            walTable.clear();
            flushFuture.complete(true);
        }
        // TODO: recover or just panic
        return flushFuture;
    }
}
