package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class Application {

    public static final String POKEMON_SPECIES_URL_QUEUE = "pokemon-species-url-queue";
    public static final String MEDIA_URL_QUEUE = "media-url-queue";
    public static final AtomicBoolean IS_RUNNING = new AtomicBoolean(Boolean.FALSE);

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        var queueService = new QueueService();
        queueService.createNewSetQueue(POKEMON_SPECIES_URL_QUEUE, 1024);
        queueService.createNewSetQueue(MEDIA_URL_QUEUE, 1024);
        var pokeApiClient = new PokeApiClient(PokeApiHost.fromDefault());
        var scheduler = new Scheduler(
                pokeApiClient,
                queueService);
        scheduler.finishedProcessingTask(TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                1,
                5,
                TimeUnit.MINUTES
        ));
        scheduler.fetchNPokemons(
                VirtualThreadConfig.newSingleThreadExecutor(),
                10);
        scheduler.mediaTask(TaskConfig.defaultConfig());
        scheduler.pokemonTask(TaskConfig.defaultConfig());
        IS_RUNNING.set(Boolean.TRUE);
        while (IS_RUNNING.get()) {
        }
    }
}