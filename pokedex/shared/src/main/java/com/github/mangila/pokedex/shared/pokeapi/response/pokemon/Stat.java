package com.github.mangila.pokedex.shared.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Stat(@JsonProperty("name") String name) {
}
