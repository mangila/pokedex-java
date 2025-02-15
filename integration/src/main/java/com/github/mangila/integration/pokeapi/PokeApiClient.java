package com.github.mangila.integration.pokeapi;

import com.github.mangila.integration.pokeapi.response.generation.GenerationResponse;
import com.github.mangila.integration.pokeapi.response.species.PokemonSpeciesResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PokeApiClient {

    private final RestClient http;

    public PokeApiClient(@Qualifier("pokeApiRestClient") RestClient http) {
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

    public PokemonSpeciesResponse fetchPokemonSpecies(String name) {
        return http.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/pokemon-species/{name}")
                        .build(name))
                .retrieve()
                .body(PokemonSpeciesResponse.class);
    }

}
