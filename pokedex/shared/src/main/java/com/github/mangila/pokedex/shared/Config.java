package com.github.mangila.pokedex.shared;

import com.github.mangila.pokedex.shared.queue.QueueName;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class Config {
    private Config() {
        throw new UnsupportedOperationException("Config class");
    }

    public static final BlockingQueue<Boolean> SHUTDOWN_QUEUE = new ArrayBlockingQueue<>(1);
    public static final String POKEAPI_HOST = "pokeapi.co";
    public static final int POKEAPI_PORT = 443;
    public static final int TLS_POOL_MAX_CONNECTIONS = 25;
    // the max count is 1025
    public static final int POKEMON_LIMIT = 1025;
    public static final QueueName TLS_CONNECTION_POOL_QUEUE = new QueueName("tls-connection-pool-queue");
    public static final QueueName POKEMON_SPECIES_URL_QUEUE = new QueueName("pokemon-species-url-queue");
    public static final QueueName POKEMON_VARIETY_URL_QUEUE = new QueueName("pokemon-variety-url-queue");
    public static final QueueName POKEMON_EVOLUTION_CHAIN_URL_QUEUE = new QueueName("pokemon-evolution-chain-url-queue");
    public static final QueueName POKEMON_SPRITES_QUEUE = new QueueName("pokemon-sprites-url-queue");
    public static final QueueName POKEMON_CRIES_QUEUE = new QueueName("pokemon-cries-url-queue");
    public static final QueueName DATABASE_WAL_WRITE_QUEUE = new QueueName("database-wal-write-queue");
    public static final QueueName DATABASE_WAL_WRITE_BIG_OBJECT_QUEUE = new QueueName("database-wal-write-big-object-queue");
    public static final QueueName DATABASE_WAL_COMPRESSION_QUEUE = new QueueName("database-wal-compression-queue");
    public static final boolean DELETE_DATABASE = Boolean.TRUE;
    public static final boolean TRUNCATE_DATABASE = Boolean.FALSE;
}
