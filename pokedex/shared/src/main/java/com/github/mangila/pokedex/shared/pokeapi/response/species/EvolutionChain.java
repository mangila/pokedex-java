package com.github.mangila.pokedex.shared.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvolutionChain(@JsonProperty("url") String url) {
}
