package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.WriteOps;
import com.github.mangila.pokedex.database.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record WalFileWriter(WalTable walTable) implements WriteOps {

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
        walTable.keys.computeIfAbsent(entry.key(), key -> new ConcurrentHashMap<>()).put(entry.field(),
                new EntryMetadata(new HashMap<>(boundaries), false));
    }

    @Override
    public void delete(Key key) {

    }

    @Override
    public void delete(Key key, Field field) {

    }
}
