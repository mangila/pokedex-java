package com.github.mangila.scheduler.service;

import com.github.mangila.integration.pokeapi.PokeApiClient;
import com.github.mangila.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.model.domain.Generation;
import com.github.mangila.scheduler.config.CacheConfig;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PokeApiService {

    private final PokeApiClient pokeApiClient;

    @Cacheable(value = CacheConfig.POKE_API_CACHE)
    public GenerationResponse fetchPokemonSpecies(Generation generation) {
        return pokeApiClient.fetchGeneration(generation.getName());
    }

}
