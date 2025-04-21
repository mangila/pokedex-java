package com.github.mangila.pokedex.shared.pokeapi.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Types(@JsonProperty("type") Type type) {
}
