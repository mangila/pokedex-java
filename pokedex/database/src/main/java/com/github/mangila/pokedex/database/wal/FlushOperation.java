package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;

import java.util.List;

record FlushOperation(
        Reason reason,
        List<Entry> entries
) {
    enum Reason {
        REPLAY,
        MANUAL,
        THRESHOLD_LIMIT
    }
}
