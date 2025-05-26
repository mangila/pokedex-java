package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;

import java.io.File;
import java.io.IOException;

public class Storage {

    private final String fileName;

    public Storage(String fileName) {
        try {
            new File(fileName).createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.fileName = fileName;
    }

    public Pokemon get(String key) {
        return null;
    }

    public void put(String key, Pokemon value) {

    }
}
