package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.EntryCollection;

record FlushOperation(
        Reason reason,
        EntryCollection entries
) {
    enum Reason {
        REPLAY,
        MANUAL,
        THRESHOLD_SCHEDULED,
        THRESHOLD_BIG_WRITE,
        THRESHOLD_LIMIT
    }
}
