package com.github.mangila.pokedex.backstage.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Varieties(
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("pokemon") Pokemon pokemon
) {}
