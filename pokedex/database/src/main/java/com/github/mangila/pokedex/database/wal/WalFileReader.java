package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Metadata;
import com.github.mangila.pokedex.database.model.Value;

import java.util.List;
import java.util.Map;

public record WalFileReader(WalTable walTable) implements ReadOps {

    @Override
    public Value get(Key key, Field field) {
        Map<Field, Metadata> fieldToMetadata = walTable.offsets.get(key);
        if (field == null) {
            return null;
        }
        Metadata metadata = fieldToMetadata.get(field);
        if (metadata == null) {
            return null;
        }
        if (metadata.tombstone()) {
            return null;
        }
        walTable.mappedBuffer.get(metadata.boundary());
        return Value.EMPTY;
    }

    @Override
    public List<Key> keys() {
        return walTable.offsets.keySet()
                .stream()
                .toList();
    }

    @Override
    public List<Field> fields(Key key) {
        return walTable.offsets.get(key)
                .keySet()
                .stream()
                .toList();
    }
}
