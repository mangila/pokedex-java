package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.scheduler.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.scheduler.pokeapi.response.allpokemons.AllPokemonsResponse;
import com.github.mangila.pokedex.scheduler.pokeapi.response.allpokemons.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
public class Scheduler {

    public static final String REDIS_POKEMON_RESULT_SET = "pokemon-result-set";
    public static final String REDIS_POKEMON_MEDIA_SET = "pokemon-media-set";

    private final PokemonTask pokemonTask;
    private final MediaTask mediaTask;
    private final PokeApiTemplate pokeApiTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

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
                .map(result -> PokemonEntry.builder()
                        .name(result.name())
                        .uri(URI.create(result.url()))
                        .build())
                .forEach(entry -> {
                    Long added = redisTemplate
                            .opsForSet()
                            .add(REDIS_POKEMON_RESULT_SET, entry);
                    if (Objects.nonNull(added) && added == 1) {
                        log.debug("Added pokemon result: {}", entry);
                    }
                });
    }

    @Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void pollPokemonResult() {
        var pokemonEntry = (PokemonEntry) redisTemplate
                .opsForSet()
                .pop(REDIS_POKEMON_RESULT_SET);
        if (Objects.nonNull(pokemonEntry)) {
            try {
                log.debug("Processing pokemon entry: {}", pokemonEntry.name());
                pokemonTask.run(pokemonEntry);
            } catch (Exception e) {
                log.error("ERR", e);
                redisTemplate.opsForSet()
                        .add(REDIS_POKEMON_RESULT_SET, pokemonEntry);
            }
        }
    }


    @Scheduled(initialDelay = 30, fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void pollPokemonMedia() {
        var mediaEntry = (MediaEntry) redisTemplate
                .opsForSet()
                .pop(REDIS_POKEMON_MEDIA_SET);
        if (Objects.nonNull(mediaEntry)) {
            try {
                log.debug("Processing media entry: {}", mediaEntry.name());
                mediaTask.run(mediaEntry);
            } catch (Exception e) {
                log.error("ERR", e);
                redisTemplate.opsForSet()
                        .add(REDIS_POKEMON_MEDIA_SET, mediaEntry);
            }
        }
    }
}
