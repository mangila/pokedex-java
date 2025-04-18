package com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Stat(@JsonProperty("name") String name) {
}
