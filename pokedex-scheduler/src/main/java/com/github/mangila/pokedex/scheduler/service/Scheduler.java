package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.scheduler.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.scheduler.pokeapi.response.allpokemons.AllPokemonsResponse;
import com.github.mangila.pokedex.scheduler.pokeapi.response.allpokemons.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
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
    public void queueAllPokemonResults() {
        var uri = URI.create("https://pokeapi.co/api/v2/pokemon-species/?&limit=1025");
        pokeApiTemplate.fetchByUrl(uri, AllPokemonsResponse.class)
                .results()
                .stream()
                .map(PokemonEntry::of)
                .forEach(entry -> queueService.add(QueueService.REDIS_POKEMON_SET, entry));
    }

    @Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void pollPokemonResult() {
        queueService.poll(QueueService.REDIS_POKEMON_SET, PokemonEntry.class, pokemonTask::run);
    }


    @Scheduled(initialDelay = 30, fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void pollPokemonMedia() {
        queueService.poll(QueueService.REDIS_POKEMON_MEDIA_SET, MediaEntry.class, mediaTask::run);
    }
}
