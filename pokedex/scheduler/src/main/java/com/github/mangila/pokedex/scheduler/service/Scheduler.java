package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.shared.pokeapi.response.allpokemons.AllPokemonsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@EnableScheduling
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true")
public class Scheduler {

    private final PokemonTask pokemonTask;
    private final MediaTask mediaTask;
    private final PokeApiTemplate pokeApiTemplate;
    private final QueueService queueService;
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void queueAllPokemonSpecies() {
        var pokeApiUri = PokeApiUri.create("https://pokeapi.co/api/v2/pokemon-species/?&limit=1025");
        pokeApiTemplate.fetchByUrl(pokeApiUri, AllPokemonsResponse.class)
                .results()
                .stream()
                .map(PokemonEntry::fromResult)
                .forEach(entry -> queueService.add(QueueService.POKEMON_QUEUE, entry));
    }

    @Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void pollPokemon() {
        processQueueEntry(QueueService.POKEMON_QUEUE, PokemonEntry.class, pokemonTask::run);
    }


    @Scheduled(initialDelay = 60, fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void pollMedia() {
        processQueueEntry(QueueService.MEDIA_QUEUE, MediaEntry.class, mediaTask::run);
    }

    /**
     * Generic method to process entries from a queue with proper error handling.
     *
     * @param queueName  the name of the queue to poll
     * @param entryClass the class type of the entry
     * @param processor  the function to process the entry
     * @param <T>        the type of the entry
     */
    private <T> void processQueueEntry(String queueName, Class<T> entryClass, Consumer<T> processor) {
        var entryOptional = queueService.poll(queueName, entryClass);
        if (entryOptional.isPresent()) {
            T entry = entryOptional.get();
            try {
                processor.accept(entry);
            } catch (Exception e) {
                log.error("Failed to process entry from queue '{}': {}", queueName, entry, e);
                queueService.add(queueName, entry);
            }
        }
    }


    @Scheduled(initialDelay = 3, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void terminateIfProcessingComplete() {
        if (areAllQueuesEmpty()) {
            log.info("All queues are empty. Shutting down application gracefully.");
            SpringApplication.exit(applicationContext, () -> 0);
        }
    }

    private boolean areAllQueuesEmpty() {
        return queueService.isEmpty(QueueService.POKEMON_QUEUE) &&
                queueService.isEmpty(QueueService.MEDIA_QUEUE);
    }
}
