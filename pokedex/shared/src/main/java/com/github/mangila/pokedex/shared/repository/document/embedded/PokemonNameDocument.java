package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.species.Names;

import java.util.List;

@lombok.Builder
public record PokemonNameDocument(
        String name,
        String language
) {

    public static PokemonNameDocument of(Names names) {
        return PokemonNameDocument.builder()
                .name(names.name())
                .language(names.language().name())
                .build();
    }

    public static List<PokemonNameDocument> of(List<Names> names) {
        return names.stream().map(PokemonNameDocument::of).toList();
    }

}
