package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationResponse(
        @JsonProperty("pokemon_species")
        List<Species> pokemonSpecies
) {
}
