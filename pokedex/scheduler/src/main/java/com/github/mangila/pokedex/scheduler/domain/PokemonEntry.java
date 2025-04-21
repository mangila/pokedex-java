package com.github.mangila.pokedex.scheduler.domain;


import com.github.mangila.pokedex.shared.pokeapi.response.allpokemons.Result;

import java.net.URI;

@lombok.Builder
public record PokemonEntry(
        String name,
        URI uri
) {

    public static PokemonEntry of(Result result) {
        return new PokemonEntry(result.name(), URI.create(result.url()));
    }

}
