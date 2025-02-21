package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites.Sprites;

public record PokemonResponse(
        @JsonProperty("name") String name,
        @JsonProperty("height") int height,
        @JsonProperty("weight") int weight,
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("cries") Cries cries,
        @JsonProperty("sprites") Sprites sprites,
        @JsonProperty("stats") Stats[] stats,
        @JsonProperty("types") Types[] types,
        @JsonProperty("abilities") Abilities[] abilities
) {
}

