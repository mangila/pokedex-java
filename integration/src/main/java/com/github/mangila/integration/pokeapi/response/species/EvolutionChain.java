package com.github.mangila.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;

public record EvolutionChain(@JsonProperty("url") URL url) {
}
