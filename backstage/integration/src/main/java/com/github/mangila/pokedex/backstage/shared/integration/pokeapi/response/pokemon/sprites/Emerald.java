package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Emerald(
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_shiny") String frontShiny
) {}
