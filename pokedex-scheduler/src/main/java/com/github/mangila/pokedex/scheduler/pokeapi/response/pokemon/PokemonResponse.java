package com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.sprites.Sprites;

import java.util.List;

public record PokemonResponse(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("height") int height,
        @JsonProperty("weight") int weight,
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("cries") Cries cries,
        @JsonProperty("sprites") Sprites sprites,
        @JsonProperty("stats") List<Stats> stats,
        @JsonProperty("types") List<Types> types
) {
}
