package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
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

    @PostConstruct
    public void queueAllPokemonResults() {
        var uri = URI.create("https://pokeapi.co/api/v2/pokemon-species/?&limit=1025");
        pokeApiTemplate.fetchByUrl(uri, AllPokemonsResponse.class)
                .results()
                .forEach(result -> {
                    Long added = redisTemplate
                            .opsForSet()
                            .add(REDIS_POKEMON_RESULT_SET, result);
                    if (Objects.nonNull(added) && added == 1) {
                        log.debug("Added pokemon result: {}", result);
                    }
                });
    }

    @Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void pollPokemonResult() {
        Result poll = (Result) redisTemplate
                .opsForSet()
                .pop(REDIS_POKEMON_RESULT_SET);
        if (Objects.nonNull(poll)) {
            try {
                log.debug("Processing pokemon species: {}", poll.name());
                pokemonTask.run(poll);
            } catch (Exception e) {
                log.error("ERR", e);
                redisTemplate.opsForSet()
                        .add(REDIS_POKEMON_RESULT_SET, poll);
            }
        }
    }


    @Scheduled(initialDelay = 30, fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void pollPokemonMedia() {
        var poll = (MediaEntry) redisTemplate
                .opsForSet()
                .pop(REDIS_POKEMON_MEDIA_SET);
        if (Objects.nonNull(poll)) {
            try {
                log.debug("Processing media: {}", poll.name());
                mediaTask.run(poll);
            } catch (Exception e) {
                log.error("ERR", e);
                redisTemplate.opsForSet()
                        .add(REDIS_POKEMON_MEDIA_SET, poll);
            }
        }
    }
}
