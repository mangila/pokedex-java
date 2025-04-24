package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.shared.pokeapi.response.allpokemons.AllPokemonsResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true")
public class Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final PokemonTask pokemonTask;
    private final MediaTask mediaTask;
    private final PokeApiTemplate pokeApiTemplate;
    private final QueueService queueService;
    private final ApplicationContext applicationContext;

    public Scheduler(PokemonTask pokemonTask,
                    MediaTask mediaTask,
                    PokeApiTemplate pokeApiTemplate,
                    QueueService queueService,
                    ApplicationContext applicationContext) {
        this.pokemonTask = pokemonTask;
        this.mediaTask = mediaTask;
        this.pokeApiTemplate = pokeApiTemplate;
        this.queueService = queueService;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void queueAllPokemonSpecies() {
        logger.info("Initializing Pokemon species queue");
        var pokeApiUri = PokeApiUri.create("https://pokeapi.co/api/v2/pokemon-species/?&limit=1025");
        logger.debug("Fetching all Pokemon species from PokeAPI: {}", pokeApiUri);

        var response = pokeApiTemplate.fetchByUrl(pokeApiUri, AllPokemonsResponse.class);
        logger.info("Retrieved {} Pokemon species from PokeAPI", response.results().size());

        response.results()
                .stream()
                .map(PokemonEntry::fromResult)
                .forEach(entry -> {
                    logger.debug("Queueing Pokemon: {}", entry.name());
                    queueService.add(QueueService.POKEMON_QUEUE, entry);
                });

        logger.info("Completed initialization of Pokemon species queue");
    }

    @Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void pollPokemon() {
        logger.debug("Scheduled task: polling Pokemon queue");
        processQueueEntry(QueueService.POKEMON_QUEUE, PokemonEntry.class, pokemonTask::run);
    }


    @Scheduled(initialDelay = 60, fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void pollMedia() {
        logger.debug("Scheduled task: polling Media queue");
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
        logger.debug("Processing queue entry: queue={}, entryType={}", queueName, entryClass.getSimpleName());
        var entryOptional = queueService.poll(queueName, entryClass);

        if (entryOptional.isPresent()) {
            T entry = entryOptional.get();
            logger.info("Processing entry from queue: queue={}, entry={}", queueName, entry);
            try {
                processor.accept(entry);
                logger.debug("Successfully processed entry: {}", entry);
            } catch (Exception e) {
                logger.error("Failed to process entry from queue: queue={}, entry={}, error={}", queueName, entry, e.getMessage(), e);
                logger.info("Re-queueing failed entry: {}", entry);
                queueService.add(queueName, entry);
            }
        } else {
            logger.trace("No entries available in queue: {}", queueName);
        }
    }


    @Scheduled(initialDelay = 3, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void terminateIfProcessingComplete() {
        logger.debug("Checking if processing is complete");
        if (areAllQueuesEmpty()) {
            logger.info("All queues are empty. Shutting down application gracefully.");
            SpringApplication.exit(applicationContext, () -> 0);
        } else {
            logger.debug("Processing not complete, queues still have entries");
        }
    }

    private boolean areAllQueuesEmpty() {
        logger.debug("Checking if all queues are empty");
        boolean pokemonQueueEmpty = queueService.isEmpty(QueueService.POKEMON_QUEUE);
        boolean mediaQueueEmpty = queueService.isEmpty(QueueService.MEDIA_QUEUE);
        logger.debug("Queue status: pokemonQueue={}, mediaQueue={}", 
                     pokemonQueueEmpty ? "empty" : "not empty", 
                     mediaQueueEmpty ? "empty" : "not empty");
        return pokemonQueueEmpty && mediaQueueEmpty;
    }
}
