package com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Sprites(
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("other") Other other
) {
}