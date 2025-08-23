package com.github.mangila.pokedex.database;

public class FileManager {

    private final WalFileManager wal;
    private final HashFileManager hash = new HashFileManager();

    public FileManager(WalFileManager wal) {
        this.wal = wal;
    }

    public WalFileManager wal() {
        return wal;
    }

    public HashFileManager hash() {
        return hash;
    }
}
