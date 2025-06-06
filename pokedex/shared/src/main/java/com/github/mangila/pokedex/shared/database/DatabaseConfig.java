package com.github.mangila.pokedex.shared.database;

/**
 * TODO
 * Compact Thread Config
 * Reader Thread Config
 * Write Thread Config
 */
public record DatabaseConfig(
        DatabaseName databaseName,
        int cacheCapacity
) {
    public DatabaseConfig {
        if (cacheCapacity <= 0) {
            throw new IllegalArgumentException("database cacheCapacity must be greater than 0");
        }
    }
}
