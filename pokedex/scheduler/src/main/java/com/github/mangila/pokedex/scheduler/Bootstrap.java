package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiMediaClient;
import com.github.mangila.pokedex.shared.https.model.PokeApiHost;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;

import java.util.concurrent.TimeUnit;

import static com.github.mangila.pokedex.scheduler.Application.MEDIA_URL_QUEUE;
import static com.github.mangila.pokedex.scheduler.Application.POKEMON_SPECIES_URL_QUEUE;

public class Bootstrap {

    public void initQueues() {
        QueueService.getInstance().createNewQueue(POKEMON_SPECIES_URL_QUEUE);
        QueueService.getInstance().createNewQueue(MEDIA_URL_QUEUE);
    }

    public PokeApiClient createPokeApiClient() {
        var pokeApiHost = PokeApiHost.fromDefault();
        var connectionPoolConfig = new TlsConnectionPoolConfig(
                pokeApiHost.host(),
                pokeApiHost.port(),
                new TlsConnectionPoolConfig.PoolConfig("pokedex-pool-1", 5),
                new TlsConnectionPoolConfig.HealthCheckConfig(10, 10, TimeUnit.SECONDS)
        );
        return new PokeApiClient(pokeApiHost, connectionPoolConfig);
    }

    public PokeApiMediaClient createMediaClient() {
        return new PokeApiMediaClient();
    }

    public Scheduler createScheduler(
            PokeApiClient pokeApiClient,
            PokeApiMediaClient mediaClient
    ) {
        return new Scheduler(pokeApiClient, mediaClient, QueueService.getInstance());
    }

    public void initScheduler(Scheduler scheduler) {
        var finishedProcessingTaskConfig = TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                1,
                5,
                TimeUnit.MINUTES
        );
        scheduler.finishedProcessingTask(finishedProcessingTaskConfig);
        scheduler.queuePokemons(VirtualThreadConfig.newSingleThreadExecutor(), 1025);
        var insertMediaTaskConfig = TaskConfig.from(
                TaskConfig.TriggerConfig.from(
                        VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                        1,
                        100,
                        TimeUnit.MILLISECONDS
                ),
                TaskConfig.WorkerConfig.from(5)
        );
        scheduler.insertMedia(insertMediaTaskConfig);
        var insertPokemonsTaskConfig = TaskConfig.from(
                TaskConfig.TriggerConfig.from(
                        VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                        1,
                        100,
                        TimeUnit.MILLISECONDS
                ),
                TaskConfig.WorkerConfig.from(5)
        );
        scheduler.insertPokemons(insertPokemonsTaskConfig);
    }
}
