package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.species.Names;

import java.util.List;

public record PokemonNameDocument(
        String name,
        String language
) {

    public static PokemonNameDocument fromNames(Names names) {
        return new PokemonNameDocument(names.name(), names.language().name());
    }

    public static List<PokemonNameDocument> fromNamesList(List<Names> names) {
        return names.stream().map(PokemonNameDocument::fromNames).toList();
    }

}
