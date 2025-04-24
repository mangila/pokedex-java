package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.species.Genera;

import java.util.List;

public record PokemonGeneraDocument(
        String genera,
        String language
) {

    public static PokemonGeneraDocument fromGenera(Genera genera) {
        return new PokemonGeneraDocument(genera.genus(), genera.language().name());
    }

    public static List<PokemonGeneraDocument> fromGeneraList(List<Genera> genera) {
        return genera.stream().map(PokemonGeneraDocument::fromGenera).toList();
    }
}
