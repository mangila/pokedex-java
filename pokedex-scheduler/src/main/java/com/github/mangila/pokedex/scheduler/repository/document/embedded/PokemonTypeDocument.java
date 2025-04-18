package com.github.mangila.pokedex.scheduler.repository.document.embedded;

import com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.Types;

import java.util.List;

@lombok.Builder
public record PokemonTypeDocument(
        String type
) {

    public static PokemonTypeDocument of(Types types) {
        return PokemonTypeDocument.builder()
                .type(types.type().name())
                .build();
    }

    public static List<PokemonTypeDocument> of(List<Types> types) {
        return types.stream().map(PokemonTypeDocument::of).toList();
    }
}
