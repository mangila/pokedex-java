package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.species.Genera;

import java.util.List;

@lombok.Builder
public record PokemonGeneraDocument(
        String genera,
        String language
) {

    public static PokemonGeneraDocument of(Genera genera) {
        return PokemonGeneraDocument.builder()
                .genera(genera.genus())
                .language(genera.language().name())
                .build();
    }

    public static List<PokemonGeneraDocument> of(List<Genera> genera) {
        return genera.stream().map(PokemonGeneraDocument::of).toList();
    }
}