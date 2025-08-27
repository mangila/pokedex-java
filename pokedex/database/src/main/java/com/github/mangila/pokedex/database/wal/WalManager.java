package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.WriteCallback;

public sealed interface WalManager permits DefaultWalManager {

    void open();

    void close();

    WriteCallback putAsync(Entry entry);

}
