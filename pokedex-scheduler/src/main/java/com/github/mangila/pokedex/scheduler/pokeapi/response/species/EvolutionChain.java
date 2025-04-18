package com.github.mangila.pokedex.scheduler.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvolutionChain(@JsonProperty("url") String url) {
}
