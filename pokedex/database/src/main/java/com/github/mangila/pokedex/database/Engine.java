package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Engine {

    private final FileManager fileManager;
    private final Cache cache;

    public Engine(FileManager fileManager, Cache cache) {
        this.fileManager = fileManager;
        this.cache = cache;
    }

    public void init() throws IOException {
        fileManager.wal().open();
    }

    public CompletableFuture<Boolean> putAsync(Key key, Value value) {
        return fileManager.wal().append(key, value)
                .thenApply(ok -> {
                    if (Boolean.TRUE.equals(ok)) {
                        cache.put(key, value);
                    }
                    return ok;
                });
    }

}
