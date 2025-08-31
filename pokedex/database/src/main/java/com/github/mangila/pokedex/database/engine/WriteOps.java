package com.github.mangila.pokedex.database.engine;

import com.github.mangila.pokedex.database.model.Entry;
import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;

public interface WriteOps {

    void put(Entry entry);

    void delete(Key key);

    void delete(Key key, Field field);

}
