package com.github.mangila.pokedex.backstage.shared.integration.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationI(
        @JsonProperty("red_blue") RedBlue redBlue,
        @JsonProperty("yellow") Yellow yellow
) {}
