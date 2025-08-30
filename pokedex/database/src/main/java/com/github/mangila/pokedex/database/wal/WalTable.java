package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Metadata;

import java.util.concurrent.ConcurrentHashMap;

public class WalTable {

    final ConcurrentHashMap<Key, ConcurrentHashMap<Field, Metadata>> offsets;
    final MappedBuffer mappedBuffer;
    private final WalFileReader read;
    private final WalFileWriter write;

    public WalTable(ConcurrentHashMap<Key, ConcurrentHashMap<Field, Metadata>> offsets,
                    MappedBuffer mappedBuffer) {
        this.offsets = offsets;
        this.mappedBuffer = mappedBuffer;
        this.read = new WalFileReader(this);
        this.write = new WalFileWriter(this);
    }

    ReadOps readOps() {
        return read;
    }

    WriteOps writeOps() {
        return write;
    }
}
