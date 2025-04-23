package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.Types;

import java.util.List;

@lombok.Builder
public record PokemonTypeDocument(
        String type
) {

    public static PokemonTypeDocument fromType(Types types) {
        return PokemonTypeDocument.builder()
                .type(types.type().name())
                .build();
    }

    public static List<PokemonTypeDocument> fromTypesList(List<Types> types) {
        return types.stream().map(PokemonTypeDocument::fromType).toList();
    }
}
