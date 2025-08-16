package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.PokeApiClient;
import com.github.mangila.pokedex.api.client.PokeApiUri;
import com.github.mangila.pokedex.api.client.response.EvolutionChainResponse;
import com.github.mangila.pokedex.api.client.response.SpeciesResponse;
import com.github.mangila.pokedex.api.client.response.VarietyResponse;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.api.model.Pokemon;
import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record InsertPokemonTask(PokeApiClient pokeApiClient,
                                QueueService queueService,
                                PokemonDatabase database) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertPokemonTask.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = VirtualThreadFactory.newSingleThreadScheduledExecutor();
    private static final ExecutorService WORKER_POOL = VirtualThreadFactory.newFixedThreadPool(10);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        LOGGER.info("Scheduling {}", name());
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> WORKER_POOL.submit(this),
                100,
                100,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean shutdown() {
        LOGGER.info("Shutting down {}", name());
     //   database.instance().close();
        var duration = Duration.ofSeconds(30);
        return VirtualThreadFactory.terminateExecutorGracefully(SCHEDULED_EXECUTOR, duration) &&
               VirtualThreadFactory.terminateExecutorGracefully(WORKER_POOL, duration);
    }

    @Override
    public void run() {
        QueueEntry queueEntry = queueService.poll(SchedulerApplication.POKEMON_SPECIES_URL_QUEUE);
        if (queueEntry == null) {
            LOGGER.debug("Queue is empty");
            return;
        }
        try {
            PokeApiUri uri = queueEntry.getDataAs(PokeApiUri.class);
            SpeciesResponse speciesResponse = pokeApiClient.fetchPokemonSpecies(uri)
                    .join();
            EvolutionChainResponse evolutionChainResponse = pokeApiClient.fetchEvolutionChain(speciesResponse.evolutionChainUrl())
                    .join();
            List<CompletableFuture<VarietyResponse>> varietyResponseFutures = speciesResponse.varietiesUrls()
                    .stream()
                    .map(pokeApiClient::fetchPokemonVariety)
                    .toList();
            CompletableFuture.allOf(varietyResponseFutures.toArray(CompletableFuture[]::new))
                    .join();
            boolean ok = database.instance()
                    .putAsync("hej", new Pokemon(1, "bulba"))
                    .join();
            if (ok) {
                LOGGER.info("Inserted pokemon");
            } else {
                throw new IllegalStateException("Db fail");
            }
        } catch (Exception e) {
            if (queueEntry.equalsMaxRetries(3)) {
                queueService.add(SchedulerApplication.POKEMON_SPECIES_URL_DL_QUEUE, queueEntry);
                return;
            }
            queueEntry.incrementFailCounter();
            queueService.add(SchedulerApplication.POKEMON_SPECIES_URL_QUEUE, queueEntry);
        }
    }
}
