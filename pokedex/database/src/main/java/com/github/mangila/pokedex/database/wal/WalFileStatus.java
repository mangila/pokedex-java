package com.github.mangila.pokedex.database.wal;

enum WalFileStatus {
    OPEN,
    CLOSED,
    FLUSHING,
    DELETED
}
