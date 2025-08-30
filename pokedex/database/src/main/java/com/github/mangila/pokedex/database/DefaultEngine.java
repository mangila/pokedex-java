package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.util.Ensure;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

final class DefaultEngine implements Engine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEngine.class);
    private volatile boolean open;
    private final FileManager fileManager;
    private final Cache cache;
    private final ScheduledExecutorService executor;

    DefaultEngine(FileManager fileManager, Cache cache) {
        this.open = false;
        this.fileManager = fileManager;
        this.cache = cache;
        this.executor = VirtualThreadFactory.newScheduledThreadPool(1024);
    }

    @Override
    public WriteCallback put(String key, String field, byte[] value) {
        Ensure.isTrue(isOpen(), "Database not open");
        Key k = new Key(key);
        Field f = new Field(field);
        Value v = new Value(value);
        return fileManager.wal().put(new Entry(k, f, v));
    }

    @Override
    public CompletableFuture<WriteCallback> putAsync(String key, String field, byte[] value) {
        return CompletableFuture.supplyAsync(() -> put(key, field, value), executor);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public synchronized void open() {
        if (isOpen()) {
            LOGGER.warn("Database already open");
            return;
        }
        open = true;
        fileManager.wal().open();
    }

    @Override
    public synchronized void close() {
        if (!isOpen()) {
            LOGGER.warn("Database already closed");
            return;
        }
        open = false;
        VirtualThreadFactory.terminateGracefully(executor);
        fileManager.wal().close();
        cache.clear();
    }

    @Override
    public void truncate() {

    }

    @Override
    public void flush() {
        fileManager.wal().flush();
    }
}
