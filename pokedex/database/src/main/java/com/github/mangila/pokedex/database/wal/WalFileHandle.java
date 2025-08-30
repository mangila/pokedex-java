package com.github.mangila.pokedex.database.wal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

class WalFileHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalFileHandle.class);
    private WalFile walFile;
    private WalTable walTable;

    void setWalFile(Path path, long size) throws IOException {
        this.walFile = new WalFile(path, size);
        this.walTable = new WalTable(new ConcurrentHashMap<>(), walFile.getMappedBuffer());
    }

    WalFile walFile() {
        return walFile;
    }

    WalTable walTable() {
        return walTable;
    }
}
