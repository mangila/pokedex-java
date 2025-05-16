package com.github.mangila.pokedex.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;


public class Application {

    public static final String POKEMON_SPECIES_URL_QUEUE = "pokemon-species-url-queue";
    public static final String MEDIA_URL_QUEUE = "media-url-queue";
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