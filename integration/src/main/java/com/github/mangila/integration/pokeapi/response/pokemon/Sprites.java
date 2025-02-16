package com.github.mangila.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;

public record Sprites(
        @JsonProperty("front_default")
        URL frontDefault,
        @JsonProperty("back_default")
        URL backDefault
) {
}
