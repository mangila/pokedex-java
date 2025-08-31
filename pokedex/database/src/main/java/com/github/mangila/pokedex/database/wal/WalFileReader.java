package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.ReadOps;
import com.github.mangila.pokedex.database.model.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public record WalFileReader(WalTable walTable) implements ReadOps {

    @Override
    public Value get(Key key, Field field) {
        ConcurrentHashMap<Field, EntryMetadata> fields = walTable.keys.get(key);
        if (fields == null) {
            return Value.EMPTY;
        }
        EntryMetadata metadata = fields.get(field);
        if (metadata == null) {
            return Value.EMPTY;
        }
        Buffer buffer = walTable.mappedBuffer.get(metadata.getBoundary(EntryMetadata.BoundaryType.VALUE));
        byte[] bytes = buffer.getValue();
        return new Value(bytes);
    }

    @Override
    public List<Key> keys() {
        return List.copyOf(walTable.keys.keySet());
    }

    @Override
    public List<Field> fields(Key key) {
        return List.of();
    }
}
