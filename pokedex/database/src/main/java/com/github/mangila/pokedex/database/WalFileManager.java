package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
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
                // TODO: recover or just panic
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
                walRotations.set(walFile.getRotation() + 1);
                WalFileHandler handler = new WalFileHandler(walFile);
                walFile.open(handler.walTable());
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
        WalFileHandler handler = new WalFileHandler(walFile);
        walFile.open(handler.walTable());
        handlerRef.set(handler);
        walRotations.incrementAndGet();
    }
}
