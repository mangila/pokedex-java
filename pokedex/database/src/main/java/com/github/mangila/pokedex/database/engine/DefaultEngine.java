package com.github.mangila.pokedex.database.engine;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.database.model.WriteCallback;
import com.github.mangila.pokedex.database.wal.WalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultEngine implements Engine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEngine.class);
    private final WalManager walManager;
    private final Cache cache;

    public DefaultEngine(WalManager walManager, Cache cache) {
        this.walManager = walManager;
        this.cache = cache;
    }

    @Override
    public void open() {
        walManager.open();
    }

    @Override
    public void close() {
        walManager.close();
        cache.clear();
    }

    @Override
    public void truncate() {

    }

    @Override
    public void flush() {
        walManager.flush();
    }

    @Override
    public Value get(Key key, Field field) {
        return walManager.get(key, field);
    }

    @Override
    public WriteCallback put(Key key, Field field, Value value) {
        return walManager.put(key, field, value);
    }
}
