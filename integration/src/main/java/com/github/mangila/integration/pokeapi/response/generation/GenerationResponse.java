package com.github.mangila.integration.pokeapi.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationResponse(
        @JsonProperty("pokemon_species")
        List<PokemonSpecies> pokemonSpecies
) {
}

