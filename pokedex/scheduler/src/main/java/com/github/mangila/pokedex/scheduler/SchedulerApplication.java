package com.github.mangila.pokedex.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;


public class SchedulerApplication {

    public static final String POKEAPI_HOST = "pokeapi.co";
    public static final int POKEAPI_PORT = 443;
    public static final String POKEMON_SPECIES_URL_QUEUE = "pokemon-species-url-queue";
    public static final String POKEMON_SPECIES_URL_DL_QUEUE = "pokemon-species-url-dl-queue";
    public static final String POKEMON_SPRITES_QUEUE = "pokemon-sprites-queue";
    public static final String POKEMON_CRIES_QUEUE = "pokemon-cries-queue";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(Boolean.FALSE);

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        Scheduler scheduler = new Scheduler(new SchedulerConfig(bootstrap.initTasks()));
        scheduler.init();
        IS_RUNNING.set(Boolean.TRUE);
        while (IS_RUNNING.get()) {

        }
        IS_RUNNING.set(Boolean.FALSE);
    }
}