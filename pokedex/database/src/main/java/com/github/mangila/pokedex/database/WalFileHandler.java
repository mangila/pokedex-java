package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
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
                walFile.delete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                LOGGER.error("Failed to delete WAL file", e);
            }
            VirtualThreadFactory.terminateGracefully(flushExecutor, Duration.ofSeconds(30));
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
                .whenComplete((status, error) -> {
                    if (error == null && status == WalAppendStatus.SUCCESS) {
                        walTable.put(hashKey, field, value);
                        if (walTable.fieldSize() >= 10 && shouldFlush()) {
                            flushLatch.countDown();
                        }
                    } else if (error == null && status == WalAppendStatus.FAILED) {
                        LOGGER.warn("Failed to write to WAL file");
                    } else {
                        LOGGER.error("ERR", error);
                    }
                });
    }

    public boolean shouldFlush() {
        return walFile.status().compareAndSet(WalFileStatus.OPEN, WalFileStatus.SHOULD_FLUSH);
    }

    public boolean isFlushing() {
        return walFile.status().get() == WalFileStatus.FLUSHING;
    }

    public void flush() throws InterruptedException {
        if (walFile.status().compareAndSet(WalFileStatus.SHOULD_FLUSH, WalFileStatus.FLUSHING)) {
            LOGGER.info("Flushing WAL file {}", walFile.getPath());
            if (walFile.channel().awaitInFlightWritesWithRetry(Duration.ofMinutes(1), 3)) {
                QueueService.getInstance().add(
                        new QueueName("hej"),
                        new QueueEntry(walTable)
                );
                walTable.clear();
            }
        }
    }
}
