package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.scheduler.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.scheduler.pokeapi.response.allpokemons.AllPokemonsResponse;
import com.github.mangila.pokedex.scheduler.pokeapi.response.allpokemons.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

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

    /**
     * Initializes and queues all Pokémon species results from the external PokéAPI into a Redis set.
     * <p>
     * This method is executed after the bean's construction and is responsible for fetching
     * a list of all Pokémon species using the {@link PokeApiTemplate#fetchByUrl(URI, Class)} method.
     * The results are then stored in the Redis set identified by the constant {@code REDIS_POKEMON_RESULT_SET}.
     * <p>
     * Each Pokémon result is added to the Redis set, and a debug log statement is generated to indicate
     * the addition of a new unique result.
     * <p>
     * Behavior details:
     * - Fetches data from the PokéAPI endpoint: https://pokeapi.co/api/v2/pokemon-species/?&limit=1025.
     * - Converts the API response to an instance of {@link AllPokemonsResponse}.
     * - Iterates over the list of {@link Result} objects from the response.
     * - For each result, it checks if the result is successfully added to the Redis set.
     * - Logs a debug message for each new unique result added to the Redis set.
     */
    @PostConstruct
    public void queueAllPokemonSpecies() {
        var uri = URI.create("https://pokeapi.co/api/v2/pokemon-species/?&limit=1025");
        pokeApiTemplate.fetchByUrl(uri, AllPokemonsResponse.class)
                .results()
                .stream()
                .map(PokemonEntry::of)
                .forEach(entry -> queueService.add(QueueService.POKEMON_QUEUE, entry));
    }

    @Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void pollPokemon() {
        var entryOptional = queueService.poll(QueueService.POKEMON_QUEUE, PokemonEntry.class);
        if (entryOptional.isPresent()) {
            try {
                pokemonTask.run(entryOptional.get());
            } catch (Exception e) {
                log.error("Failed to process entry", e);
                queueService.add(QueueService.POKEMON_QUEUE, entryOptional.get());
            }
        }
    }


    @Scheduled(initialDelay = 60, fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void pollMedia() {
        var entryOptional = queueService.poll(QueueService.MEDIA_QUEUE, MediaEntry.class);
        if (entryOptional.isPresent()) {
            try {
                mediaTask.run(entryOptional.get());
            } catch (Exception e) {
                log.error("Failed to process entry", e);
                queueService.add(QueueService.MEDIA_QUEUE, entryOptional.get());
            }
        }
    }

    @Scheduled(initialDelay = 3, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void shutdown() {
        if (queueService.isEmpty(QueueService.POKEMON_QUEUE) &&
                queueService.isEmpty(QueueService.MEDIA_QUEUE)) {
            System.exit(0);
        }
    }
}
