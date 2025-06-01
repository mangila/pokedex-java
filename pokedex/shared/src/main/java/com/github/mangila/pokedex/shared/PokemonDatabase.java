package com.github.mangila.pokedex.shared;

import com.github.mangila.pokedex.shared.database.Database;
import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Bill Pugh Instance for the Pokemon database
 */
public class PokemonDatabase {

    private static final Logger log = LoggerFactory.getLogger(PokemonDatabase.class);

    private static DatabaseConfig config;

    private final Database<Pokemon> database;

    private PokemonDatabase(DatabaseConfig config) {
        this.database = new Database<>(config, () -> Pokemon.DEFAULT_INSTANCE);
    }

    public static void configure(DatabaseConfig config) {
        Objects.requireNonNull(config, "DatabaseConfig must not be null");
        if (PokemonDatabase.config != null) {
            throw new IllegalStateException("DatabaseConfig is already configured");
        }
        log.info("Configuring PokemonDatabase with {}", config);
        PokemonDatabase.config = config;
    }

    private static final class Holder {
        private static final PokemonDatabase INSTANCE = new PokemonDatabase(config);
    }

    public static PokemonDatabase getInstance() {
        Objects.requireNonNull(config, "PokemonDatabase must be configured");
        return Holder.INSTANCE;
    }

    public Database<Pokemon> get() {
        return database;
    }
}
