package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;
import com.github.mangila.pokedex.database.model.WriteCallback;

sealed interface Engine permits DefaultEngine {

    void open();

    void close();

    void truncate();

    void flush();

    Value get(Key key, Field field);

    WriteCallback put(Key key, Field field, Value value);
}
