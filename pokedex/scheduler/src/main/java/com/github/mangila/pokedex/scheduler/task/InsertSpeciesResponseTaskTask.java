package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.api.client.pokeapi.response.SpeciesResponse;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.shared.Config;
import com.github.mangila.pokedex.shared.queue.Queue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record InsertSpeciesResponseTaskTask(PokeApiClient pokeApiClient,
                                            Queue queue,
                                            PokemonDatabase database) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSpeciesResponseTaskTask.class);

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_POOL = VirtualThreadFactory.newScheduledThreadPool(10);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        LOGGER.info("Scheduling {}", name());
        SCHEDULED_EXECUTOR_POOL.scheduleAtFixedRate(this,
                100,
                100,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean shutdown() {
        LOGGER.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        return VirtualThreadFactory.terminateGracefully(SCHEDULED_EXECUTOR_POOL, duration);
    }

    @Override
    public void run() {
        QueueEntry queueEntry = queue.poll();
        if (queueEntry == null) {
            LOGGER.debug("Queue is empty");
            return;
        }
        try {
            PokeApiUri uri = queueEntry.unwrapAs(PokeApiUri.class);
            SpeciesResponse speciesResponse = pokeApiClient.fetchPokemonSpecies(uri)
                    .join();
            QueueService.getInstance()
                    .add(Config.POKEMON_EVOLUTION_CHAIN_URL_QUEUE, new QueueEntry(speciesResponse.evolutionChainUrl()));
            speciesResponse.varietiesUrls()
                    .forEach(url -> QueueService.getInstance()
                            .add(Config.POKEMON_VARIETY_URL_QUEUE, new QueueEntry(url)));
            LOGGER.info("#{} {}", speciesResponse.id(), speciesResponse.name());
            database.instance().engine()
                    .putAsync("pokemon::" + speciesResponse.id(),
                            "name",
                            speciesResponse.name().getBytes(Charset.defaultCharset()));
        } catch (Exception e) {
            LOGGER.error("ERR", e);
            if (queueEntry.equalsMaxRetries(3)) {
                queue.addDlq(queueEntry);
                return;
            }
            queueEntry.incrementFailCounter();
            queue.add(queueEntry);
        }
    }
}
