package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiamondPearl(
        @JsonProperty("back_default") String backDefault,
        @JsonProperty("back_female") String backFemale,
        @JsonProperty("back_shiny") String backShiny,
        @JsonProperty("back_shiny_female") String backShinyFemale,
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_female") String frontFemale,
        @JsonProperty("front_shiny") String frontShiny,
        @JsonProperty("front_shiny_female") String frontShinyFemale
) {}
