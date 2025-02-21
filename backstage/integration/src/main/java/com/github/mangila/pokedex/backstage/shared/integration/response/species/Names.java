package com.github.mangila.pokedex.backstage.shared.integration.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Names(
        @JsonProperty("language") Language language,
        @JsonProperty("name") String name
) {}
