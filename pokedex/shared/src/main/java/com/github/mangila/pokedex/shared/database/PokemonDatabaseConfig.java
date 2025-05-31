package com.github.mangila.pokedex.shared.database;

import com.github.mangila.pokedex.shared.database.internal.file.FileName;

public record PokemonDatabaseConfig(
        FileName fileName,
        int cacheCapacity
) {

    public PokemonDatabaseConfig {
        if (cacheCapacity <= 0) {
            throw new IllegalArgumentException("database cacheCapacity must be greater than 0");
        }
    }

}
