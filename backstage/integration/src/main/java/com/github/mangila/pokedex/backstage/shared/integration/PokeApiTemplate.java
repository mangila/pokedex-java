package com.github.mangila.pokedex.backstage.shared.integration;

import com.github.mangila.pokedex.backstage.shared.integration.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.backstage.shared.integration.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.shared.integration.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.backstage.shared.integration.response.species.SpeciesResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PokeApiTemplate {

    private final RestClient http;

    public PokeApiTemplate(@Qualifier("pokeApiClient") RestClient http) {
        this.http = http;
    }

    public GenerationResponse fetchGeneration(String generation) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/generation/{generation}")
                        .build(generation))
                .retrieve()
                .body(GenerationResponse.class);
    }

    public SpeciesResponse fetchSpecies(String name) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon-species/{name}")
                        .build(name))
                .retrieve()
                .body(SpeciesResponse.class);
    }

    public EvolutionChainResponse fetchEvolutionChain(String evolutionChainId) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/evolution-chain/{evolutionChainId}")
                        .build(evolutionChainId))
                .retrieve()
                .body(EvolutionChainResponse.class);
    }

    public PokemonResponse fetchPokemon(String name) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon/{name}")
                        .build(name))
                .retrieve()
                .body(PokemonResponse.class);
    }
}
