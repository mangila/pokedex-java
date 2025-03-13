package com.github.mangila.pokedex.api.graphql.model;

import java.util.List;

public record PokemonSpecies(
        int id,
        String name,
        String generation,
        List<PokemonName> names,
        List<PokemonDescription> descriptions,
        List<PokemonGenera> genera,
        List<PokemonEvolution> evolutions,
        List<Pokemon> varieties,
        PokemonSpecial special
) {
}
