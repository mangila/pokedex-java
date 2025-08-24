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
import java.util.concurrent.ExecutorService;

public class WalFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandler.class);
    private final WalFile walFile;
    private final WalTable walTable;
    private final ExecutorService flushExecutor;

    public WalFileHandler(WalFile walFile) {
        this.walFile = walFile;
        this.walTable = new WalTable(new ConcurrentSkipListMap<>(Comparator.comparing(HashKey::value)));
        this.flushExecutor = VirtualThreadFactory.newSingleThreadExecutor();
        flushExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (shouldFlush()) {
                        flush();
                    } else {
                        Thread.sleep(50);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to flush WAL file", e);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
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
                .whenComplete((status, error) -> {
                    if (error == null && status == WalAppendStatus.SUCCESS) {
                        walTable.put(hashKey, field, value);
                        if (walFile.channel().writeCount() > 10) {
                            walFile.status().compareAndSet(WalFileStatus.OPEN, WalFileStatus.SHOULD_FLUSH);
                        }
                    } else if (error == null && status == WalAppendStatus.FAILED) {
                        LOGGER.warn("Failed to write to WAL file");
                    } else {
                        LOGGER.error("ERR", error);
                    }
                });
    }

    public void flush() throws IOException {
        if (walFile.status().compareAndSet(WalFileStatus.SHOULD_FLUSH, WalFileStatus.FLUSHING)) {
            LOGGER.info("Flushing WAL file {}", walFile.getPath());
            try {
                walFile.channel().awaitInFlightWrites(Duration.ofMinutes(1));
            } catch (Exception e) {
                LOGGER.error("Failed to flush WAL file", e);
            }
            QueueService.getInstance().add(
                    new QueueName("hej"),
                    new QueueEntry(walTable)
            );
            walTable.clear();
            walFile.delete();
        }
    }

    public boolean shouldFlush() {
        return walFile.status().get() == WalFileStatus.SHOULD_FLUSH;
    }

    public boolean isFlushing() {
        return walFile.status().get() == WalFileStatus.FLUSHING;
    }
}
