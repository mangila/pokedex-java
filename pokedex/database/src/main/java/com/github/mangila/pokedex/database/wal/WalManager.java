package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.*;

public sealed interface WalManager permits DefaultWalManager {

    void open();

    void close();

    void flush();

    WriteCallback put(Entry entry);

    Value get(Key key, Field field);
}
