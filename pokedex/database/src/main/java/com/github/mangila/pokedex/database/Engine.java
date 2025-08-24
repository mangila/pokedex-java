package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.concurrent.CompletableFuture;

public class Engine {
    private final FileManager fileManager;
    private final Cache cache;

    public Engine(FileManager fileManager, Cache cache) {
        this.fileManager = fileManager;
        this.cache = cache;
    }

    public CompletableFuture<Boolean> putAsync(Key key, Field field, Value value) {
        return fileManager.wal()
                .putAsync(key, field, value)
                .copy();
    }

}
