package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class WalFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileManager.class);
    private final DatabaseName databaseName;
    private final ExecutorService shutdownHandlerExecutor = VirtualThreadFactory.newSingleThreadExecutor();
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

    CompletableFuture<Boolean> putAsync(Key key, Field field, Value value) {
        if (shouldRotate()) {
            try {
                closeCurrentHandlerAsync();
                rotate();
                rotate.set(false);
            } catch (IOException e) {
                // TODO: recover or just panic
                LOGGER.error("Failed to rotate WAL file", e);
                return CompletableFuture.failedFuture(e);
            }
        }
        return handlerRef.get()
                .appendAsync(key, field, value)
                .thenApply(walAppendStatus -> walAppendStatus == WalAppendStatus.SUCCESS);
    }

    private void replay() throws IOException {
        try (DirectoryStream<Path> walFiles = Files.newDirectoryStream(Path.of("."),
                entry -> {
                    String name = entry.getFileName().toString();
                    return name.startsWith(databaseName.value()) && name.endsWith(".wal");
                })) {
            for (Path path : walFiles) {
                LOGGER.info("Replaying WAL file {}", path);
                try {
                    WalFile walFile = new WalFile(path);
                    walRotations.set(walFile.getRotation() + 1);
                    WalFileHandler handler = new WalFileHandler(walFile);
                    walFile.open(handler.walTable());
                    handler.flush();
                    handler.closeAndDelete(Duration.ofMillis(1));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        rotate();
    }

    private void rotate() throws IOException {
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

    private boolean shouldRotate() {
        return handlerRef.get().isFlushing() && rotate.compareAndSet(false, true);
    }

    private void closeCurrentHandlerAsync() {
        WalFileHandler current = handlerRef.get();
        if (current != null) {
            current.flushCompletion().whenCompleteAsync((v, t) -> {
                try {
                    current.closeAndDelete(Duration.ofSeconds(30));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, shutdownHandlerExecutor);
        }
    }
}
