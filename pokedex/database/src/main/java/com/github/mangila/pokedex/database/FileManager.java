package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.wal.WalManager;

class FileManager {

    private final WalManager wal;
    private final HashFileManager hash = new HashFileManager();

    FileManager(DatabaseConfig config) {
        this.wal = new WalManager();
    }

    WalManager wal() {
        return wal;
    }

    HashFileManager hash() {
        return hash;
    }
}
