package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.wal.DefaultWalFileManager;
import com.github.mangila.pokedex.database.wal.WalFileManager;

class FileManager {

    private final WalFileManager wal;
    private final HashFileManager hash = new HashFileManager();

    FileManager(DatabaseConfig config) {
        this.wal = new DefaultWalFileManager(config.databaseName(), config.walConfig());
    }

    WalFileManager wal() {
        return wal;
    }

    HashFileManager hash() {
        return hash;
    }
}
