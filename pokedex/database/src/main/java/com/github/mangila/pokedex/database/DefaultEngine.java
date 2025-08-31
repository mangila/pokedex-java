package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

final class DefaultEngine implements Engine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEngine.class);
    private final FileManager fileManager;
    private final Cache cache;

    DefaultEngine(FileManager fileManager, Cache cache) {
        this.fileManager = fileManager;
        this.cache = cache;
    }

    @Override
    public void open() {
        fileManager.wal().open();
    }

    @Override
    public void close() {
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

    @Override
    public Value get(Key key, Field field) {
        return fileManager.wal().get(key, field);
    }

    @Override
    public WriteCallback put(Key key, Field field, Value value) {
        return fileManager.wal().put(new Entry(key, field, value));
    }
}
