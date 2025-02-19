package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.generation.service.PokeApiService;
import com.github.mangila.pokedex.backstage.shared.integration.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.shared.integration.response.generation.Species;
import com.github.mangila.pokedex.backstage.shared.model.Generation;
import com.github.mangila.pokedex.backstage.shared.model.RedisQueue;
import com.github.mangila.pokedex.backstage.shared.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
public class GenerationTask implements Task {

    Logger log = LoggerFactory.getLogger(GenerationTask.class);
    private final PokeApiService pokeApiService;
    private final RedisTemplate<String, String> redisTemplate;

    public GenerationTask(PokeApiService pokeApiService, RedisTemplate<String, String> redisTemplate) {
        this.pokeApiService = pokeApiService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        EnumSet.allOf(Generation.class)
                .stream()
                .peek(generation -> log.info("Generation push to Queue: {}", generation.getName()))
                .map(pokeApiService::fetchGeneration)
                .map(GenerationResponse::pokemonSpecies)
                .flatMap(List::stream)
                .map(Species::name)
                .forEach(name -> redisTemplate.opsForSet().add(RedisQueue.GENERATION_QUEUE.name(), name));
    }
}
