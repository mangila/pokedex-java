package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Crystal(
        @JsonProperty("back_default") String backDefault,
        @JsonProperty("back_shiny") String backShiny,
        @JsonProperty("back_shiny_transparent") String backShinyTransparent,
        @JsonProperty("back_transparent") String backTransparent,
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_shiny") String frontShiny,
        @JsonProperty("front_shiny_transparent") String frontShinyTransparent,
        @JsonProperty("front_transparent") String frontTransparent
) {}
