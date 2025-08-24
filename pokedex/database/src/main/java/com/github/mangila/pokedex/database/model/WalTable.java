package com.github.mangila.pokedex.database.model;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

public record WalTable(ConcurrentSkipListMap<Key, ConcurrentSkipListMap<Field, Value>> map) {
    public void put(Key key, Field field, Value value) {
        var created = new ConcurrentSkipListMap<Field, Value>(Comparator.comparing(Field::value));
        var existing = map.putIfAbsent(key, created);
        var table = (existing != null) ? existing : created;
        table.put(field, value);
    }

    public void clear() {
        map.clear();
    }

    public int hashKeySize() {
        return map.size();
    }

    public int fieldSize() {
        return map.values()
                .stream()
                .mapToInt(ConcurrentSkipListMap::size)
                .sum();
    }
}
