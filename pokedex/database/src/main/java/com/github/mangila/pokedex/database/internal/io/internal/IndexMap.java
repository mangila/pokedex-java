package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.internal.model.IndexEntry;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.model.Key;

import java.util.Map;

public record IndexMap(Map<Key, Offset> keyToOffset) {

    public int size() {
        return keyToOffset.size();
    }

    public Offset get(Key key) {
        return keyToOffset.get(key);
    }

    public void put(IndexEntry indexEntry) {
        keyToOffset.put(indexEntry.key(), indexEntry.offset());
    }

    public void clear() {
        keyToOffset.clear();
    }

    public void putAll(Map<Key, Offset> map) {
        keyToOffset.putAll(map);
    }
}
