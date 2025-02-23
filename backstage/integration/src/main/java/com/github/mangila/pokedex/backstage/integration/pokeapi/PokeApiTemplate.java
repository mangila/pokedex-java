package com.github.mangila.pokedex.backstage.integration.pokeapi;

import com.github.mangila.pokedex.backstage.integration.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.species.SpeciesResponse;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PokeApiTemplate {

    private final RestClient http;

    public PokeApiTemplate(@Qualifier("pokeApiClient") RestClient http) {
        this.http = http;
    }

    @RegisterReflectionForBinding(GenerationResponse.class)
    public GenerationResponse fetchGeneration(String generation) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/generation/{generation}")
                        .build(generation))
                .retrieve()
                .body(GenerationResponse.class);
    }

    @RegisterReflectionForBinding(SpeciesResponse.class)
    public SpeciesResponse fetchSpecies(String name) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon-species/{name}")
                        .build(name))
                .retrieve()
                .body(SpeciesResponse.class);
    }

    @RegisterReflectionForBinding(EvolutionChainResponse.class)
    public EvolutionChainResponse fetchEvolutionChain(String evolutionChainId) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/evolution-chain/{evolutionChainId}")
                        .build(evolutionChainId))
                .retrieve()
                .body(EvolutionChainResponse.class);
    }

    @RegisterReflectionForBinding(PokemonResponse.class)
    public PokemonResponse fetchPokemon(String name) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon/{name}")
                        .build(name))
                .retrieve()
                .body(PokemonResponse.class);
    }
}
