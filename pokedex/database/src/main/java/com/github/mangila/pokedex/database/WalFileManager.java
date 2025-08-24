package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.queue.QueueName;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class WalFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileManager.class);
    private final DatabaseName databaseName;
    private final AtomicReference<WalFileHandler> handlerRef = new AtomicReference<>();
    private final AtomicBoolean rotate = new AtomicBoolean(false);
    private final AtomicLong walRotations = new AtomicLong(1);

    public WalFileManager(DatabaseName databaseName) {
        this.databaseName = databaseName;
        QueueService.getInstance().createNewQueue(new QueueName("hej"));
        try {
            replay();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    CompletableFuture<Boolean> appendAsync(HashKey hashKey, Field field, Value value) {
        if (shouldRotate()) {
            try {
                rotate();
                rotate.set(false);
            } catch (IOException e) {
                LOGGER.error("Failed to rotate WAL file", e);
                return CompletableFuture.failedFuture(e);
            }
        }
        return handlerRef.get()
                .appendAsync(hashKey, field, value)
                .thenApply(walAppendStatus -> walAppendStatus == WalAppendStatus.SUCCESS);
    }

    private boolean shouldRotate() {
        return handlerRef.get().isFlushing() && rotate.compareAndSet(false, true);
    }

    void replay() throws IOException {
        try (DirectoryStream<Path> walFiles = Files.newDirectoryStream(Path.of("."),
                entry -> {
                    String name = entry.getFileName().toString();
                    return name.startsWith(databaseName.value()) && name.endsWith(".wal");
                })) {
            for (Path path : walFiles) {
                LOGGER.info("Replaying WAL file {}", path);
                WalFile walFile = new WalFile(path);
                walRotations.set(walFile.getRotation());
                walFile.open();
                WalFileHandler handler = new WalFileHandler(walFile);
                try {
                    handler.flush();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        rotate();
    }

    void rotate() throws IOException {
        String name = databaseName.value()
                .concat("-" + walRotations.get())
                .concat(".wal");
        Path path = Path.of(name);
        WalFile walFile = new WalFile(path);
        walFile.open();
        WalFileHandler handler = new WalFileHandler(walFile);
        handlerRef.set(handler);
        walRotations.incrementAndGet();
    }
}
