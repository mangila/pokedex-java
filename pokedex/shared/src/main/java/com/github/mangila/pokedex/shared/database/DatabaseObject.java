package com.github.mangila.pokedex.shared.database;

import java.io.IOException;
import java.io.Serializable;

public interface DatabaseObject<T> extends Serializable {

    byte[] serialize() throws IOException;

    T deserialize(byte[] data) throws IOException;

    double schemaVersion();
}
