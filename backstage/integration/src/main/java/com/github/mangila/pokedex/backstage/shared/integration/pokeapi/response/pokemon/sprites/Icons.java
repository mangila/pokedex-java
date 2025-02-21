package com.github.mangila.pokedex.backstage.shared.integration.pokeapi.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Icons(
        @JsonProperty("front_default") String frontDefault,
        @JsonProperty("front_female") String frontFemale
) {}
