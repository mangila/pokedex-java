package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;

public interface WriteOps {

    void put(Entry entry);

}
