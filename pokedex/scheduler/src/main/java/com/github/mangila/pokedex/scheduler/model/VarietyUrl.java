package com.github.mangila.pokedex.scheduler.model;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;

public record VarietyUrl(
        PokeApiUri pokeApiUri,
        String key) {
}
