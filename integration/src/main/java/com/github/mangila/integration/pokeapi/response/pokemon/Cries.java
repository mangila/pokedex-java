package com.github.mangila.integration.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;

public record Cries(
        @JsonProperty("latest")
        URL latest,
        @JsonProperty("legacy")
        URL legacy
) {
}
