package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

final class DefaultEngine implements Engine {

    private volatile boolean open;
    private final FileManager fileManager;
    private final Cache cache;
    private final ExecutorService executor;

    DefaultEngine(FileManager fileManager, Cache cache) {
        this.open = false;
        this.fileManager = fileManager;
        this.cache = cache;
        this.executor = VirtualThreadFactory.newFixedThreadPool(256);
    }

    @Override
    public WriteCallback put(String key, String field, byte[] value) {
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
    public void open() {
        open = true;
        fileManager.wal().open();
    }

    @Override
    public void close() {
        open = false;
        fileManager.wal().close();
        VirtualThreadFactory.terminateGracefully(executor);
        cache.clear();
    }

    @Override
    public void flush() {
        fileManager.wal().flush();
    }
}
