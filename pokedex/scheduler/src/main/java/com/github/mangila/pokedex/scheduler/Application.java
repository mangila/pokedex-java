package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiMediaClient;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
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
        queueService.createNewQueue(POKEMON_SPECIES_URL_QUEUE);
        queueService.createNewQueue(MEDIA_URL_QUEUE);
        var pokeApiHost = PokeApiHost.fromDefault();
        var pokeApiClient = new PokeApiClient(PokeApiHost.fromDefault(),
                new TlsConnectionPoolConfig(
                        pokeApiHost.host(),
                        pokeApiHost.port(),
                        5,
                        new TlsConnectionPoolConfig.HealthCheckConfig(10, 10, TimeUnit.SECONDS)
                ));
        var mediaClient = new PokeApiMediaClient();
        var scheduler = new Scheduler(
                pokeApiClient,
                mediaClient,
                queueService);
        scheduler.finishedProcessingTask(TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                1,
                5,
                TimeUnit.MINUTES
        ));
        var pokemonCount = 1025;
        scheduler.queuePokemons(
                VirtualThreadConfig.newSingleThreadExecutor(),
                pokemonCount);
        scheduler.insertMedia(TaskConfig.from(
                TaskConfig.TriggerConfig.from(
                        VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                        1,
                        100,
                        TimeUnit.MILLISECONDS
                ),
                TaskConfig.WorkerConfig.from(10)
        ));
        scheduler.insertPokemons(TaskConfig.from(
                TaskConfig.TriggerConfig.from(
                        VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                        1,
                        100,
                        TimeUnit.MILLISECONDS
                ),
                TaskConfig.WorkerConfig.from(10)
        ));
        IS_RUNNING.set(Boolean.TRUE);
        while (IS_RUNNING.get()) {
        }
    }
}