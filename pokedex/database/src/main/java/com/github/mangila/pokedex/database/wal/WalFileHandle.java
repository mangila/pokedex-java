package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class WalFileHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandle.class);
    private WalFile walFile;
    private Map<Key, Map<Field, Metadata>> offsets;
    private Write write;
    private Read read;

    void setWalFile(Path path, long size) throws IOException {
        this.walFile = new WalFile(path, size);
        this.offsets = new HashMap<>();
        this.write = new Write(this);
        this.read = new Read(this);
    }

    private record Read(WalFileHandle walFileHandle) {

        @Nullable
        ByteBuffer get(Key key, Field field) {
            Map<Field, Metadata> fieldToMetadata = walFileHandle.offsets.get(key);
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
            return walFileHandle.walFile.getMappedBuffer()
                    .get(metadata.boundary());
        }

    }

    private record Write(WalFileHandle walFileHandle) {

        void put(Entry entry) {
            int startPosition = walFileHandle.walFile
                    .getMappedBuffer()
                    .position();
            walFileHandle.walFile
                    .getMappedBuffer()
                    .fill(entry);
            int endPosition = walFileHandle.walFile
                    .getMappedBuffer()
                    .position();
            walFileHandle.offsets.computeIfAbsent(entry.key(), key -> new HashMap<>())
                    .put(entry.field(), new Metadata(
                            new OffsetBoundary(startPosition, endPosition), false
                    ));
        }
    }


}
