package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
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

public class WalFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandler.class);
    private final WalFile walFile;
    private final WalTable walTable;
    private final ExecutorService flushExecutor;
    private final CountDownLatch flushLatch;

    public WalFileHandler(WalFile walFile) {
        this.walFile = walFile;
        this.walTable = new WalTable(new ConcurrentSkipListMap<>(Comparator.comparing(HashKey::value)));
        this.flushExecutor = VirtualThreadFactory.newSingleThreadExecutor();
        this.flushLatch = new CountDownLatch(1);
        flushExecutor.submit(() -> {
            try {
                flushLatch.await();
                flush();
                walFile.status().set(WalFileStatus.FLUSHED);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Interrupted while waiting for flush", e);
            }
        });
    }

    public CompletableFuture<WalAppendStatus> appendAsync(HashKey hashKey, Field field, Value value) {
        var walAppendFuture = new CompletableFuture<WalAppendStatus>();
        int bufferSize = hashKey.getBufferSize() + field.getBufferSize() + value.getBufferSize();
        Buffer writeBuffer = Buffer.from(bufferSize);
        writeBuffer.put(hashKey);
        writeBuffer.put(field);
        writeBuffer.put(value);
        writeBuffer.flip();
        walFile.channel().write(writeBuffer, walAppendFuture);
        return walAppendFuture
                .thenApply(status -> {
                    if (status == WalAppendStatus.SUCCESS) {
                        walTable.put(hashKey, field, value);
                    } else if (status == WalAppendStatus.FAILED) {
                        LOGGER.warn("Failed to write to WAL file");
                    }
                    if (walTable.fieldSize() >= 10 && shouldFlush()) {
                        flushLatch.countDown();
                    }
                    return status;
                });
    }

    public boolean hasFlushed() {
        return walFile.status().get() == WalFileStatus.FLUSHED;
    }

    public WalTable walTable() {
        return walTable;
    }

    private boolean shouldFlush() {
        return walFile.status().compareAndSet(WalFileStatus.OPEN, WalFileStatus.FLUSHING);
    }

    public void closeAndDelete(Duration duration) throws IOException {
        VirtualThreadFactory.terminateGracefully(flushExecutor, duration);
        walFile.close();
        walFile.delete();
    }

    public void flush() throws InterruptedException {
        if (walFile.channel().awaitInFlightWritesWithRetry(Duration.ofMinutes(1), 3)) {
            LOGGER.info("Flushing WAL file {}", walFile.getPath());
            // TODO: send to disk via future or smt
            walTable.clear();
        }
        // TODO: recover or just panic
    }
}
