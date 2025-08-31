package com.github.mangila.pokedex.database.engine;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.List;

public interface ReadOps {

    Value get(Key key, Field field);

    List<Key> keys();

    List<Field> fields(Key key);

}
