package com.github.mangila.pokedex.database.model;

import java.util.concurrent.ConcurrentSkipListMap;

public record MemTable(ConcurrentSkipListMap<Key, Value> map) {

    public Value getOrEmpty(Key key) {
        return map.getOrDefault(key, Value.EMPTY);
    }

    public void put(Key key, Value value) {
        map.put(key, value);
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }
}
