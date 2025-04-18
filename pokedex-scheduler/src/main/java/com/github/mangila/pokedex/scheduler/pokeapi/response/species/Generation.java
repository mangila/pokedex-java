package com.github.mangila.pokedex.scheduler.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Generation(@JsonProperty("name") String name) {
}
