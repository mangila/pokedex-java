package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiUri;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import com.github.mangila.pokedex.shared.util.VirtualThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record QueuePokemonsTask(PokeApiClient pokeApiClient,
                                QueueService queueService,
                                int pokemonCount) implements Task {

    private static final Logger log = LoggerFactory.getLogger(QueuePokemonsTask.class);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = VirtualThreadConfig.newSingleThreadScheduledExecutor();

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {
        var future = SCHEDULED_EXECUTOR.schedule(this, 1, TimeUnit.SECONDS);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        shutdown();
    }

    @Override
    public boolean shutdown() {
        log.info("Shutting down {}", name());
        var duration = Duration.ofSeconds(30);
        return VirtualThreadUtils.terminateExecutorGracefully(SCHEDULED_EXECUTOR, duration);
    }

    @Override
    public void run() {
        var request = new JsonRequest(
                "GET",
                String.format("/api/v2/pokemon-species/?&limit=%d", pokemonCount),
                List.of());
        pokeApiClient.getJsonAsync(request)
                .join()
                .ifPresentOrElse(jsonResponse -> {
                    PokeApiClientUtil.ensureSuccessStatusCode(jsonResponse);
                    var results = jsonResponse.body()
                            .getArray("results");
                    log.info("Found {} pokemon species", results.size());
                    var queueEntries = results.values()
                            .stream()
                            .map(JsonValue::getObject)
                            .map(jsonObject -> jsonObject.getString("url"))
                            .map(PokeApiUri::fromString)
                            .map(QueueEntry::new)
                            .peek(queueEntry -> log.debug("Queue entry {}", queueEntry.data()))
                            .map(queueEntry -> queueService.add(SchedulerApplication.POKEMON_SPECIES_URL_QUEUE, queueEntry))
                            .toList();
                    log.info("Queued {} pokemon species", queueEntries.size());
                }, () -> {
                    throw new RuntimeException("Failed to fetch pokemon species");
                });
    }
}
