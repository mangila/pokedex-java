package com.github.mangila.pokedex.backstage.shared.integration.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Stat(
        @JsonProperty("name") String name
) {}
