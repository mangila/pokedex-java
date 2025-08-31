package com.github.mangila.pokedex.api.db;

import com.github.mangila.pokedex.database.DefaultDatabase;
import com.github.mangila.pokedex.database.config.DatabaseConfig;
import com.github.mangila.pokedex.database.model.DatabaseName;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PokemonDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonDatabase.class);

    private static final DatabaseConfig DEFAULT_CONFIG = DatabaseConfig.builder()
            .databaseName(new DatabaseName("pokedex"))
            .build();
    private static DatabaseConfig config;

    private static final class Holder {
        private static final PokemonDatabase INSTANCE = new PokemonDatabase(config);
    }

    public static PokemonDatabase getInstance() {
        Ensure.notNull(config, "PokemonDatabase must be configured");
        return Holder.INSTANCE;
    }

    public static void configureDefaultSettings() {
        LOGGER.info("Configuring PokemonDatabase with default config");
        configure(DEFAULT_CONFIG);
    }

    public static void configure(DatabaseConfig config) {
        Ensure.notNull(config, "DatabaseConfig must not be null");
        if (PokemonDatabase.config != null) {
            throw new IllegalStateException("DatabaseConfig is already configured");
        }
        LOGGER.info("Configuring PokemonDatabase with {}", config);
        PokemonDatabase.config = config;
    }

    private final DefaultDatabase instance;

    private PokemonDatabase(DatabaseConfig config) {
        this.instance = new DefaultDatabase(config);
    }

    public DefaultDatabase instance() {
        return instance;
    }
}