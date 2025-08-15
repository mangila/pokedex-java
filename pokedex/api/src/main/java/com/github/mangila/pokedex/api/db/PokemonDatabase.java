package com.github.mangila.pokedex.api.db;

import com.github.mangila.pokedex.api.model.Pokemon;
import com.github.mangila.pokedex.database.Database;
import com.github.mangila.pokedex.database.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record PokemonDatabase(Database<Pokemon> database) {
    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonDatabase.class);

    public static PokemonDatabase init(DatabaseConfig config) {
        LOGGER.info("Initializing PokemonDatabase with {}", config);
        Database<Pokemon> database = new Database<>(config, () -> Pokemon.DEFAULT_INSTANCE);
        return new PokemonDatabase(database);
    }

    public Database<Pokemon> get() {
        return database;
    }
}
