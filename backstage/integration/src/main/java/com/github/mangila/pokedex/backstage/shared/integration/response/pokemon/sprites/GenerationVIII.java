package com.github.mangila.pokedex.backstage.shared.integration.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationVIII(
        @JsonProperty("icons") Icons icons
) {}
