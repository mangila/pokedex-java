package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

record WalTable(Map<Key, Map<Field, Value>> table) {
    void put(Key key, Field field, Value value) {
        table.compute(key, (k, fieldMap) -> {
            if (fieldMap == null) {
                fieldMap = new java.util.concurrent.ConcurrentHashMap<>();
            }
            fieldMap.put(field, value);
            return fieldMap;
        });
    }

    public void remove(List<Entry> entries) {
        entries.forEach(entry -> {
            Key key = entry.key();
            Field field = entry.field();
            Value value = entry.value();
            Map<Field, Value> fieldMap = table.get(key);
            if (fieldMap != null) {
                Value existingValue = fieldMap.get(field);
                if (existingValue != null && Arrays.equals(existingValue.value(), value.value())) {
                    fieldMap.remove(field);
                    if (fieldMap.isEmpty()) {
                        table.remove(key);
                    }
                }
            }
        });
    }

    public void clear() {
        table.clear();
    }
}
