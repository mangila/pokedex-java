package com.github.mangila.pokedex.backstage.shared.integration.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationV(
        @JsonProperty("black_white") BlackWhite blackWhite
) {}
