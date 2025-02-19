package com.github.mangila.pokedex.backstage.generation.service;

import com.github.mangila.pokedex.backstage.shared.integration.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.shared.integration.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.shared.model.Generation;
import com.github.mangila.pokedex.backstage.shared.model.RedisPrefix;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PokeApiService {

    private final PokeApiTemplate pokeApiTemplate;

    public PokeApiService(PokeApiTemplate pokeApiTemplate) {
        this.pokeApiTemplate = pokeApiTemplate;
    }

    @Cacheable(cacheNames = RedisPrefix.GENERATION, key = "#generation.getName()")
    public GenerationResponse fetchGeneration(Generation generation) {
        return pokeApiTemplate.fetchGeneration(generation.getName());
    }

}
