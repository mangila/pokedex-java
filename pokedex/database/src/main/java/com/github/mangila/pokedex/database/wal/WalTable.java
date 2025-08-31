package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.ReadOps;
import com.github.mangila.pokedex.database.WriteOps;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.EntryMetadata;

import java.util.concurrent.ConcurrentHashMap;

public class WalTable {

    final ConcurrentHashMap<Key, ConcurrentHashMap<Field, EntryMetadata>> keys;
    final MappedBuffer mappedBuffer;
    private final WalFileReader read;
    private final WalFileWriter write;

    public WalTable(ConcurrentHashMap<Key, ConcurrentHashMap<Field, EntryMetadata>> keys,
                    MappedBuffer mappedBuffer) {
        this.keys = keys;
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
