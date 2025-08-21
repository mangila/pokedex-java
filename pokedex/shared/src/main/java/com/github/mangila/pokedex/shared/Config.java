package com.github.mangila.pokedex.shared;

import com.github.mangila.pokedex.shared.queue.QueueName;

public final class Config {
    private Config() {
        throw new UnsupportedOperationException("Config class");
    }

    public static final String POKEAPI_HOST = "pokeapi.co";
    public static final int POKEAPI_PORT = 443;
    // the max count is 1025
    public static final int POKEMON_LIMIT = 100;
    public static final QueueName POKEMON_SPECIES_URL_QUEUE = new QueueName("pokemon-species-url-queue");
    public static final QueueName POKEMON_SPRITES_QUEUE = new QueueName("pokemon-sprites-queue");
    public static final QueueName POKEMON_CRIES_QUEUE = new QueueName("pokemon-cries-queue");
    public static final boolean DELETE_DATABASE = Boolean.TRUE;
    public static final boolean TRUNCATE_DATABASE = Boolean.FALSE;
}
