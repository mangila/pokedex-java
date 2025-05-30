package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.SchedulerApplication;
import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.model.primitives.PokeApiUri;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public record QueuePokemonsTask(PokeApiClient pokeApiClient,
                                QueueService queueService,
                                int pokemonCount) implements Task {

    private static final Logger log = LoggerFactory.getLogger(QueuePokemonsTask.class);

    @Override
    public String getTaskName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public TaskConfig getTaskConfig() {
        var trigger = TaskConfig.TriggerConfig.from(
                VirtualThreadConfig.newSingleThreadScheduledExecutor(),
                TaskConfig.TaskType.ONE_OFF,
                10,
                10,
                TimeUnit.SECONDS);
        var workers = TaskConfig.WorkerConfig.from(1);
        return TaskConfig.from(trigger, workers);
    }

    @Override
    public void run() {
        try {
            var request = new JsonRequest(
                    "GET",
                    String.format("/api/v2/pokemon-species/?&limit=%d", pokemonCount),
                    List.of());
            var l = pokeApiClient.getJson(request)
                    .map(PokeApiClientUtil::ensureSuccessStatusCode)
                    .map(JsonResponse::body)
                    .map(jsonTree -> jsonTree.getArray("results"))
                    .map(array -> array.values().stream()
                            .map(JsonValue::getObject)
                            .map(jsonObject -> jsonObject.getString("url"))
                            .map(PokeApiUri::fromString)
                            .map(QueueEntry::new)
                            .peek(queueEntry -> log.debug("Queue entry {}", queueEntry.data()))
                            .map(queueEntry -> queueService.add(SchedulerApplication.POKEMON_SPECIES_URL_QUEUE, queueEntry)))
                    .orElseThrow()
                    .toList();
            log.info("Queued {} pokemon species", l.size());
        } catch (Exception e) {
            log.error("ERR", e);
        }
    }
}
