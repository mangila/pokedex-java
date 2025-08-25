package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.concurrent.CompletableFuture;

class DefaultEngine implements Engine {
    private final FileManager fileManager;
    private final Cache cache;

    public DefaultEngine(FileManager fileManager, Cache cache) {
        this.fileManager = fileManager;
        this.cache = cache;
    }

    @Override
    public CompletableFuture<Boolean> putAsync(String key, String field, byte[] value) {
        Key k = new Key(key);
        Field f = new Field(field);
        Value v = new Value(value);
        return fileManager.wal()
                .putAsync(k, f, v)
                .copy();
    }
}
