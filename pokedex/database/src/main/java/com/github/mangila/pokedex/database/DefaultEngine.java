package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;

import java.time.Duration;
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
        this.executor = VirtualThreadFactory.newFixedThreadPool(64);
    }

    @Override
    public CompletableFuture<WriteCallback> putAsync(String key, String field, byte[] value) {
        Key k = new Key(key);
        Field f = new Field(field);
        Value v = new Value(value);
        return CompletableFuture.supplyAsync(() -> fileManager.wal().putAsync(new Entry(k, f, v)), executor);
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
        VirtualThreadFactory.terminateGracefully(executor, Duration.ofSeconds(30));
        fileManager.wal().close();
        cache.clear();
    }
}
