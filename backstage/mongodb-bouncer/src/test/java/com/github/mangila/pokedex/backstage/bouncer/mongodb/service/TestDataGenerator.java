package com.github.mangila.pokedex.backstage.bouncer.mongodb.service;

import com.github.mangila.pokedex.backstage.bouncer.mongodb.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded.PokemonSpecialDocument;

import java.util.Collections;

final class TestDataGenerator {

    public static PokemonSpeciesDocument createDefaultPokemonSpeciesDocument() {
        return new PokemonSpeciesDocument(
                1,
                "bulbasaur",
                "generation-i",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                new PokemonSpecialDocument(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE)
        );
    }

}
