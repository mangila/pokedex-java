package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.EntryCollection;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Staging table for WAL writes.
 *
 * Staged in memory until we have a durable write to the WAL file.
 */
record WalTable(ConcurrentHashMap<Key, ConcurrentHashMap<Field, Value>> table) {
    void put(Key key, Field field, Value value) {
        table.computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .put(field, value);
    }

    void remove(EntryCollection entries) {
        entries.collection().forEach(entry -> {
            table.compute(entry.key(), (key, fieldMap) -> {
                if (fieldMap != null) {
                    fieldMap.compute(entry.field(), (field, existingValue) -> {
                        if (existingValue != null) {
                            byte[] flushedValue = entry.value().value();
                            byte[] current = existingValue.value();
                            // fixme: bottleneck
                            if (Arrays.equals(current, flushedValue)) {
                                return null;
                            }
                        }
                        return existingValue;
                    });

                    if (fieldMap.isEmpty()) {
                        return null;
                    }
                }
                return fieldMap;
            });
        });
    }

    public void clear() {
        table.clear();
    }
}
