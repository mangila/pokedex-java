package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.engine.WriteOps;
import com.github.mangila.pokedex.database.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record WalTableWriter(WalTable walTable) implements WriteOps {

    @Override
    public void put(Entry entry) {
        MappedBuffer mappedBuffer = walTable.mappedBuffer;
        int start = mappedBuffer.position();
        mappedBuffer.put(entry.key());
        int keyEndPosition = mappedBuffer.position();
        mappedBuffer.put(entry.field());
        int fieldEndPosition = mappedBuffer.position();
        mappedBuffer.put(entry.value());
        int valueEndPosition = mappedBuffer.position();
        mappedBuffer.put((byte) 0);
        int end = mappedBuffer.position();
        Map<EntryMetadata.BoundaryType, OffsetBoundary> boundaries = Map.of(
                EntryMetadata.BoundaryType.KEY, new OffsetBoundary(start, keyEndPosition),
                EntryMetadata.BoundaryType.FIELD, new OffsetBoundary(keyEndPosition, fieldEndPosition),
                EntryMetadata.BoundaryType.VALUE, new OffsetBoundary(fieldEndPosition, valueEndPosition),
                EntryMetadata.BoundaryType.TOMBSTONE, new OffsetBoundary(valueEndPosition, end)
        );
        walTable.keys.computeIfAbsent(entry.key(), key -> new ConcurrentHashMap<>())
                .put(entry.field(), new EntryMetadata(new HashMap<>(boundaries), false));
    }

    @Override
    public void delete(Key key) {
        // TODO: iterate over fields and set tombstone flag in metadata and in buffer
    }

    @Override
    public void delete(Key key, Field field) {
        // TODO: set tombstone flag in metadata and in buffer
        walTable.keys.computeIfPresent(key, (k, v) -> {
            var f = v.get(field);
            f.setTombstone(true);
            return v.isEmpty() ? null : v;
        });
    }
}
