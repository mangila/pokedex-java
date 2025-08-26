package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.concurrent.CompletableFuture;

final class DefaultEngine implements Engine {

    private volatile boolean open;
    private final FileManager fileManager;
    private final Cache cache;

    DefaultEngine(FileManager fileManager, Cache cache) {
        this.fileManager = fileManager;
        this.cache = cache;
        this.open = false;
    }

    @Override
    public CompletableFuture<Void> putAsync(String key, String field, byte[] value) {
        Key k = new Key(key);
        Field f = new Field(field);
        Value v = new Value(value);
        return fileManager.wal()
                .putAsync(k, f, v)
                .copy();
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
        cache.clear();
    }
}
