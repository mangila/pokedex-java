package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.model.Generation;
import com.github.mangila.pokedex.backstage.model.Task;
import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation.Species;
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

    public GenerationTask(RedisBouncerClient redisBouncerClient,
                          PokeApiTemplate pokeApiTemplate) {
        this.redisBouncerClient = redisBouncerClient;
        this.pokeApiTemplate = pokeApiTemplate;
    }

    @Override
    public void run(String[] args) {
        EnumSet.allOf(Generation.class)
                .stream()
                .map(Generation::getName)
                .peek(generation -> log.info("Generation push to Queue: {}", generation))
                .map(pokeApiTemplate::fetchGeneration)
                .map(GenerationResponse::pokemonSpecies)
                .flatMap(List::stream)
                .map(Species::name)
                .forEach(name -> log.info("Generation name: {}", name));
    }
}
