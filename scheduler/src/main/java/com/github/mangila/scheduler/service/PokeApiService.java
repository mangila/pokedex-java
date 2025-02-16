package com.github.mangila.scheduler.service;

import com.github.mangila.integration.pokeapi.PokeApiClient;
import com.github.mangila.integration.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.integration.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.integration.pokeapi.response.species.EvolutionChain;
import com.github.mangila.integration.pokeapi.response.species.PokemonSpeciesResponse;
import com.github.mangila.integration.pokeapi.response.species.Variety;
import com.github.mangila.model.domain.Generation;
import com.github.mangila.model.domain.PokemonName;
import com.github.mangila.scheduler.config.CacheConfig;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PokeApiService {

    private final PokeApiClient pokeApiClient;

    @Cacheable(cacheNames = CacheConfig.POKE_API_GENERATION, key = "#generation.name()")
    public GenerationResponse fetchGeneration(Generation generation) {
        return pokeApiClient.fetchGeneration(generation.getName());
    }

    @Cacheable(value = CacheConfig.POKE_API_SPECIES, key = "#pokemonName.name")
    public PokemonSpeciesResponse fetchPokemonSpecies(PokemonName pokemonName) {
        return pokeApiClient.fetchPokemonSpecies(pokemonName.getName());
    }

    @Cacheable(value = CacheConfig.POKE_API_EVOLUTION_CHAIN, keyGenerator = "evolutionChainCacheKeyGenerator")
    public EvolutionChainResponse fetchEvolutionChain(EvolutionChain evolutionChain) {
        var url = evolutionChain.url();
        String path = url.getPath();
        String[] pathSegments = path.split("/");
        String lastSegment = pathSegments[pathSegments.length - 1];
        return pokeApiClient.fetchEvolutionChain(lastSegment);
    }

    @Cacheable(value = CacheConfig.POKE_API_POKEMON, key = "#variety.pokemon().name()")
    public PokemonResponse fetchPokemon(Variety variety) {
        var name = variety.pokemon().name();
        return pokeApiClient.fetchPokemon(name);
    }
}
