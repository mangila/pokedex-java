package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.HashKey;
import com.github.mangila.pokedex.database.model.Value;

import java.util.concurrent.CompletableFuture;

public class Engine {
    private final FileManager fileManager;
    private final Cache cache;

    public Engine(FileManager fileManager, Cache cache) {
        this.fileManager = fileManager;
        this.cache = cache;
    }

    public CompletableFuture<Boolean> appendAsync(HashKey hashKey, Field field, Value value) {
        return fileManager.wal().appendAsync(hashKey, field, value)
                .whenComplete((ok, error) -> {
                    if (error == null && Boolean.TRUE.equals(ok)) {
                        cache.put(hashKey, value);
                    }
                });
    }

}
