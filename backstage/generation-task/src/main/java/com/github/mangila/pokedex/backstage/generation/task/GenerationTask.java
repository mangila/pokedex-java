package com.github.mangila.pokedex.backstage.generation.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation.Species;
import com.github.mangila.pokedex.backstage.model.Generation;
import com.github.mangila.pokedex.backstage.model.PokemonName;
import com.github.mangila.pokedex.backstage.model.RedisQueueName;
import com.github.mangila.pokedex.backstage.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
public class GenerationTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(GenerationTask.class);
    private final RedisBouncerClient redisBouncerClient;
    private final PokeApiTemplate pokeApiTemplate;
    private final ObjectMapper objectMapper;

    public GenerationTask(RedisBouncerClient redisBouncerClient,
                          PokeApiTemplate pokeApiTemplate,
                          ObjectMapper objectMapper) {
        this.redisBouncerClient = redisBouncerClient;
        this.pokeApiTemplate = pokeApiTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String[] args) {
        EnumSet.allOf(Generation.class)
                .stream()
                .map(Generation::getName)
                .peek(generation -> log.info("Generation push to Queue: {}", generation))
                .map(generationName -> {
                    var cacheValue = redisBouncerClient.get(generationName, GenerationResponse.class);
                    if (cacheValue.isEmpty()) {
                        var response = pokeApiTemplate.fetchGeneration(generationName);
                        redisBouncerClient.set(generationName, response.toJson(objectMapper));
                        return response;
                    }
                    return cacheValue.get();
                })
                .map(GenerationResponse::pokemonSpecies)
                .flatMap(List::stream)
                .map(Species::name)
                .map(PokemonName::new)
                .map(pokemonName -> pokemonName.toJson(objectMapper))
                .forEach(json -> redisBouncerClient.add(RedisQueueName.GENERATION_QUEUE.toString(), json));
    }
}
