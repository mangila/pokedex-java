package com.github.mangila.pokedex.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;


public class SchedulerApplication {

    public static final String POKEMON_SPECIES_URL_QUEUE = "value-species-url-queue";
    public static final String POKEMON_SPECIES_URL_DL_QUEUE = "value-species-url-dl-queue";
    public static final String POKEMON_SPRITES_QUEUE = "value-sprites-queue";
    public static final String POKEMON_CRIES_QUEUE = "value-cries-queue";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(Boolean.FALSE);

    public static void main(String[] args) {
        var bootstrap = new Bootstrap();
        bootstrap.configureJsonParser();
        bootstrap.configurePokeApiClient();
        bootstrap.configurePokeApiMediaClient();
        bootstrap.configurePokemonDatabase();
        bootstrap.configureScheduler();
        bootstrap.initQueues();
        bootstrap.initScheduler();
        IS_RUNNING.set(Boolean.TRUE);
        while (IS_RUNNING.get()) {
        }
    }
}