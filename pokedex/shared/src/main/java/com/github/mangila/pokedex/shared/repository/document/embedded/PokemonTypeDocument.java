package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.Types;

import java.util.List;

public record PokemonTypeDocument(
        String type
) {

    public static PokemonTypeDocument fromType(Types types) {
        return new PokemonTypeDocument(types.type().name());
    }

    public static List<PokemonTypeDocument> fromTypesList(List<Types> types) {
        return types.stream().map(PokemonTypeDocument::fromType).toList();
    }
}
