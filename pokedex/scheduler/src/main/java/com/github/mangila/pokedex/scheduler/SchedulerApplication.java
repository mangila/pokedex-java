package com.github.mangila.pokedex.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;


public class SchedulerApplication {

    public static final String POKEMON_SPECIES_URL_QUEUE = "pokemon-species-url-queue";
    public static final String POKEMON_SPECIES_URL_DL_QUEUE = "pokemon-species-url-dl-queue";
    public static final String POKEMON_SPRITES_QUEUE = "pokemon-sprites-queue";
    public static final String POKEMON_CRIES_QUEUE = "pokemon-cries-queue";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(Boolean.FALSE);

    public static void main(String[] args) {
        var bootstrap = new Bootstrap();
        bootstrap.initQueues();
        var scheduler = bootstrap.createScheduler(
                bootstrap.createPokeApiClient(),
                bootstrap.createMediaClient()
        );
        bootstrap.initScheduler(scheduler);
        IS_RUNNING.set(Boolean.TRUE);
        while (IS_RUNNING.get()) {
        }
    }
}