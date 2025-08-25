package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.wal.DefaultWalFileManager;
import com.github.mangila.pokedex.database.wal.WalFileManager;

class FileManager {

    private final WalFileManager wal;
    private final HashFileManager hash = new HashFileManager();

    public FileManager(DatabaseConfig config) {
        this.wal = new DefaultWalFileManager(config.databaseName(), config.walConfig());
    }

    public WalFileManager wal() {
        return wal;
    }

    public HashFileManager hash() {
        return hash;
    }
}
