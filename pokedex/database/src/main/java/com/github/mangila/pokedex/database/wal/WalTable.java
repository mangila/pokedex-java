package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

record WalTable(ConcurrentSkipListMap<Key, ConcurrentSkipListMap<Field, Value>> map) {
    void put(Key key, Field field, Value value) {
        var created = new ConcurrentSkipListMap<Field, Value>(Comparator.comparing(Field::value));
        var existing = map.putIfAbsent(key, created);
        var table = (existing != null) ? existing : created;
        table.put(field, value);
    }

    void clear() {
        map.clear();
    }

    int hashKeySize() {
        return map.size();
    }

    int fieldSize() {
        return map.values()
                .stream()
                .mapToInt(ConcurrentSkipListMap::size)
                .sum();
    }
}
