package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Gold(
        @JsonProperty("back_default") String backDefault,
        @JsonProperty("back_shiny") String backShiny,
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_shiny") String frontShiny,
        @JsonProperty("front_transparent") String frontTransparent
) {}
