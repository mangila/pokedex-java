package com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UltraSunUltraMoon(
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_female") String frontFemale,
        @JsonProperty("front_shiny") String frontShiny,
        @JsonProperty("front_shiny_female") String frontShinyFemale
) {}
