package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.cache.LruCache;
import com.github.mangila.pokedex.shared.database.internal.Engine;
import com.github.mangila.pokedex.shared.database.internal.Storage;

public class Database {

    private static Database instance;

    private final Engine engine;

    private Database() {
        this.engine = new Engine(
                new LruCache(),
                new Storage());
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public boolean put(String key, byte[] value) {
        return engine.put(key, value);
    }

    public byte[] get(String key) {
        return engine.get(key);
    }

}
