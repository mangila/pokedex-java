package com.github.mangila.pokedex.shared.database;

public record PokemonDatabaseConfig(
        String fileName,
        int cacheCapacity
) {
}
