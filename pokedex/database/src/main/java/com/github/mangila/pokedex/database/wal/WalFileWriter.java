package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.WriteOps;
import com.github.mangila.pokedex.database.model.*;

import java.util.concurrent.ConcurrentHashMap;

public record WalFileWriter(WalTable walTable) implements WriteOps {

    @Override
    public void put(Entry entry) {
        int startPosition = walTable.mappedBuffer.position();
        walTable.mappedBuffer.fill(entry);
        int endPosition = walTable.mappedBuffer.position();
        walTable.offsets.computeIfAbsent(entry.key(), key -> new ConcurrentHashMap<>())
                .put(entry.field(), new Metadata(
                        new OffsetBoundary(startPosition, endPosition), false
                ));
    }

    @Override
    public void delete(Key key) {

    }

    @Override
    public void delete(Key key, Field field) {

    }
}
