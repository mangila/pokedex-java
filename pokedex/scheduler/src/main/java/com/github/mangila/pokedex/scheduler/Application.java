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

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static final String POKEMON_SPECIES_URL_QUEUE = "pokemon-species-url-queue";
    public static final String MEDIA_URL_QUEUE = "media-url-queue";
    public static final AtomicBoolean isRunning = new AtomicBoolean(Boolean.FALSE);

    public static void main(String[] args) {
        var queueService = new QueueService();
        queueService.createNewSetQueue(POKEMON_SPECIES_URL_QUEUE, 1024);
        queueService.createNewSetQueue(MEDIA_URL_QUEUE, 1024);
        var scheduler = new Scheduler(
                new PokeApiClient(new PokeApiHost("pokeapi.co", 443)),
                queueService);
        scheduler.finishedProcessingTask(TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                1,
                5,
                TimeUnit.MINUTES
        ));
        scheduler.fetchAllPokemonsTask(VirtualThreadConfig.newVirtualThreadPerTaskExecutor());
        scheduler.mediaTask(TaskConfig.defaultConfig());
        scheduler.pokemonTask(TaskConfig.defaultConfig());
        isRunning.set(Boolean.TRUE);
        while (isRunning.get()) {
        }
    }
}