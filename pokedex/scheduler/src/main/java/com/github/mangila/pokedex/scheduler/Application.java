package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static final AtomicBoolean isRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        var queueService = new QueueService();
        queueService.createNewSetQueue("pokemon", 1024);
        queueService.createNewSetQueue("media", 1024);
        var scheduler = new Scheduler(
                new PokeApiClient(new PokeApiHost("pokeapi.co", 443)),
                queueService);
        scheduler.fetchAllPokemonsTask(VirtualThreadConfig.newVirtualThreadPerTaskExecutor());
        scheduler.mediaTask(VirtualThreadConfig.newScheduledThreadPool(10));
        scheduler.pokemonTask(VirtualThreadConfig.newSingleThreadScheduledExecutor());
        isRunning.set(Boolean.TRUE);
        while (isRunning.get()) {
        }
    }
}