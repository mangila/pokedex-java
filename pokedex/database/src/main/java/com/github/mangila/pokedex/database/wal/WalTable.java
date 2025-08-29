package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WalTable implements ReadOps, WriteOps {

    private final ConcurrentHashMap<Key, Map<Field, Metadata>> offsets;
    private final MappedBuffer mappedBuffer;
    private final Read read;
    private final Write write;

    public WalTable(ConcurrentHashMap<Key, Map<Field, Metadata>> offsets, MappedBuffer mappedBuffer) {
        this.offsets = offsets;
        this.mappedBuffer = mappedBuffer;
        this.read = new Read(this);
        this.write = new Write(this);
    }

    @Override
    public Value get(Key key, Field field) {
        ByteBuffer buffer = read.get(key, field);
        return Value.EMPTY;
    }

    @Override
    public List<Key> keys() {
        return offsets.keySet()
                .stream()
                .toList();
    }

    @Override
    public List<Field> fields(Key key) {
        return offsets.get(key)
                .keySet()
                .stream()
                .toList();
    }

    private record Read(WalTable walTable) {
        @Nullable
        ByteBuffer get(Key key, Field field) {
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
            return walTable.mappedBuffer.get(metadata.boundary());
        }

    }

    private record Write(WalTable walTable) {

        void put(Entry entry) {
            int startPosition = walTable.mappedBuffer.position();
            walTable.mappedBuffer.fill(entry);
            int endPosition = walTable.mappedBuffer.position();
            walTable.offsets.computeIfAbsent(entry.key(), key -> new HashMap<>())
                    .put(entry.field(), new Metadata(
                            new OffsetBoundary(startPosition, endPosition), false
                    ));
        }
    }
}
