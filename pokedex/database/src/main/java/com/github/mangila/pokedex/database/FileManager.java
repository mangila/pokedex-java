package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.wal.DefaultWalManager;

class FileManager {

    private final DefaultWalManager wal;

    FileManager(DatabaseConfig config) {
        this.wal = new DefaultWalManager();
        wal.open();
    }

    DefaultWalManager wal() {
        return wal;
    }
}
