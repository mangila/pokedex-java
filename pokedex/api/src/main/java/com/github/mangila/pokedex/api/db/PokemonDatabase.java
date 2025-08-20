package com.github.mangila.pokedex.api.db;

import com.github.mangila.pokedex.api.model.Pokemon;
import com.github.mangila.pokedex.database.Database;
import com.github.mangila.pokedex.database.DatabaseConfig;
import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.shared.cache.lru.LruCacheConfig;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PokemonDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonDatabase.class);

    private static final DatabaseConfig DEFAULT_CONFIG = new DatabaseConfig(
            new DatabaseName("pokedex"),
            new LruCacheConfig(50)
    );
    private static DatabaseConfig config;

    private final Database<Pokemon> instance;

    private PokemonDatabase(DatabaseConfig config) {
        this.instance = new Database<>(config, () -> Pokemon.DEFAULT_INSTANCE);
        instance.init();
    }

    public static void defaultConfig() {
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

    private static final class Holder {
        private static final PokemonDatabase INSTANCE = new PokemonDatabase(config);
    }

    public static PokemonDatabase getInstance() {
        Ensure.notNull(config, "PokemonDatabase must be configured");
        return Holder.INSTANCE;
    }

    public Database<Pokemon> db() {
        return instance;
    }
}