package com.github.mangila.pokedex.backstage.shared.integration.response.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationResponse(
        @JsonProperty("pokemon_species")
        List<Species> pokemonSpecies
) {
}
