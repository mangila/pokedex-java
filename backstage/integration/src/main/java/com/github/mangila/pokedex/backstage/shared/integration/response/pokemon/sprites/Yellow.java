package com.github.mangila.pokedex.backstage.shared.integration.response.pokemon.sprites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Yellow(
        @JsonProperty("back_default") String backDefault,
        @JsonProperty("back_gray") String backGray,
        @JsonProperty("back_transparent") String backTransparent,
        @JsonProperty("front_default") String frontDefault
) {}
