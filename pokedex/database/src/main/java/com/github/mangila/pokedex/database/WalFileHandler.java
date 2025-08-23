package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class WalFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandler.class);
    private final WalFile walFile;

    public WalFileHandler(WalFile walFile) {
        this.walFile = walFile;
    }

    public CompletableFuture<WalAppendStatus> appendAsync(HashKey hashKey, Field field, Value value) {
        var walAppendFuture = new CompletableFuture<WalAppendStatus>();
        int bufferSize = hashKey.getBufferSize() + field.getBufferSize() + value.getBufferSize();
        Buffer writeBuffer = Buffer.from(bufferSize);
        writeBuffer.put(hashKey);
        writeBuffer.put(field);
        writeBuffer.put(value);
        writeBuffer.flip();
        int bytesToWrite = writeBuffer.value().remaining();
        long position = walFile.position().getAndAdd(bytesToWrite);
        var attachment = new WalFileChannel.Attachment(
                walAppendFuture,
                position,
                bytesToWrite,
                writeBuffer
        );
        walFile.channel().write(attachment);
        return walAppendFuture
                .whenComplete((status, error) -> {
                    if (error == null && status == WalAppendStatus.SUCCESS) {
                        walFile.walTable().put(hashKey, field, value);
                        walFile.appendCount().incrementAndGet();
                    } else if (error == null && status == WalAppendStatus.FAILED) {
                        LOGGER.warn("Failed to write to WAL file");
                    } else {
                        LOGGER.error("ERR", error);
                    }
                });
    }

    public void flush() throws IOException {
        if (walFile.status().compareAndSet(WalFileStatus.FLUSHING, WalFileStatus.OPEN)) {
            LOGGER.info("Flushing WAL file {}", walFile.getPath());
            QueueService.getInstance().add(
                    new QueueName("hej"),
                    new QueueEntry(walFile.walTable())
            );
            walFile.walTable().clear();
            walFile.delete();
        }
    }

    public boolean isFlushing() {
        return walFile.status().get() == WalFileStatus.FLUSHING;
    }
}
