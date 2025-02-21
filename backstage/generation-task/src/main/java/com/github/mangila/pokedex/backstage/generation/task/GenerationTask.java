package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.cache.config.RedisCacheNames;
import com.github.mangila.pokedex.backstage.cache.config.RedisQueue;
import com.github.mangila.pokedex.backstage.cache.service.QueueService;
import com.github.mangila.pokedex.backstage.model.Generation;
import com.github.mangila.pokedex.backstage.model.Task;
import com.github.mangila.pokedex.backstage.shared.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.generation.Species;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
public class GenerationTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(GenerationTask.class);
    private final QueueService queueService;
    private final PokeApiTemplate pokeApiTemplate;

    public GenerationTask(QueueService queueService,
                          PokeApiTemplate pokeApiTemplate) {
        this.queueService = queueService;
        this.pokeApiTemplate = pokeApiTemplate;
    }

    @Override
    public void run(String[] args) {
        EnumSet.allOf(Generation.class)
                .stream()
                .peek(generation -> log.info("Generation push to Queue: {}", generation.getName()))
                .map(this::fetchGeneration)
                .map(GenerationResponse::pokemonSpecies)
                .flatMap(List::stream)
                .map(Species::name)
                .forEach(name -> queueService.add(RedisQueue.GENERATION_QUEUE.toString(), name));
    }

    @Cacheable(value = RedisCacheNames.GENERATION, key = "#generation")
    public GenerationResponse fetchGeneration(Generation generation) {
        return pokeApiTemplate.fetchGeneration(generation.getName());
    }
}
