package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.database.model.WriteCallback;

public sealed interface WalManager permits DefaultWalManager {

    void open();

    void close();

    void flush();

    Value get(Key key, Field field);

    WriteCallback put(Key key, Field field, Value value);

    WriteCallback delete(Key key, Field field);

    WriteCallback delete(Key key);
}
