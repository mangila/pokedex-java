package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.scheduler.Application;
import com.github.mangila.pokedex.shared.https.client.PokeApiClient;
import com.github.mangila.pokedex.shared.https.client.PokeApiClientUtil;
import com.github.mangila.pokedex.shared.https.model.JsonRequest;
import com.github.mangila.pokedex.shared.https.model.JsonResponse;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public record PokemonTask(PokeApiClient pokeApiClient,
                          QueueService queueService) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);

    @Override
    public void run() {
        var poll = queueService.poll()
                .apply(Application.POKEMON_SPECIES_URL_QUEUE);
        if (poll.isEmpty()) {
            log.debug("Queue is empty");
            return;
        }
        var queueEntry = poll.get();
        var url = (PokeApiUri) queueEntry.data();
        log.debug("Queue entry {}", url);
        try {
            var tree = pokeApiClient.getJson()
                    .andThen(Optional::orElseThrow)
                    .andThen(PokeApiClientUtil::ensureSuccessStatusCode)
                    .andThen(JsonResponse::getBody)
                    .apply(new JsonRequest("GET",
                            url.getPath(),
                            List.of()));
            var evolutionChainFuture = pokeApiClient.getJsonAsync()
                    .apply(new JsonRequest("GET",
                            tree.getObject("evolution_chain")
                                    .getString("url"),
                            List.of()));
            var varietyFutures = tree.getArray("varieties")
                    .values()
                    .stream()
                    .map(jsonValue -> new JsonRequest("GET",
                            jsonValue.getObject()
                                    .getObject("pokemon")
                                    .getString("url"),
                            List.of()))
                    .map(jsonRequest -> pokeApiClient.getJsonAsync()
                            .apply(jsonRequest))
                    .toList();

            var evolutionChain = evolutionChainFuture.get();
         //   log.info(evolutionChain.get().toString());
            for (var varietyFuture : varietyFutures) {
                if (varietyFuture.isDone()) {
                    var variety = varietyFuture.get();
             //       log.info(variety.get().toString());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
