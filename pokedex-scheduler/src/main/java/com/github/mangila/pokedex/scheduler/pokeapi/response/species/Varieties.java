package com.github.mangila.pokedex.scheduler.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Varieties(@JsonProperty("pokemon") Pokemon pokemon) {
}
