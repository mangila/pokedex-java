package com.github.mangila.pokedex.shared.database;

public record PokemonDatabaseConfig(
        String fileName,
        int cacheCapacity
) {

    public PokemonDatabaseConfig {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("database fileName must not be null or blank");
        }
        if (!fileName.endsWith(".pokemon")) {
            throw new IllegalArgumentException("database fileName must end with .pokemon");
        }
        if (cacheCapacity <= 0) {
            throw new IllegalArgumentException("database cacheCapacity must be greater than 0");
        }
    }

}
