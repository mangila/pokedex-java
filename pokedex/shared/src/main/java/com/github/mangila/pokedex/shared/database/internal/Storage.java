package com.github.mangila.pokedex.shared.database.internal;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Storage {

    private static final Logger log = LoggerFactory.getLogger(Storage.class);
    private final String fileName;

    public Storage(String fileName) {
        try {
            var exist = new File(fileName).createNewFile();
            if (!exist) {
                log.info("Database - {} - already exists", fileName);
            } else {
                log.info("Database - {} - created", fileName);
            }
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
