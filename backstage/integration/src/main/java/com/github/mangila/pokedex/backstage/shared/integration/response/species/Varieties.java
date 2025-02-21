package com.github.mangila.pokedex.backstage.shared.integration.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Varieties(
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("pokemon") Pokemon pokemon
) {}
