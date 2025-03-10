package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

public record PokemonDescriptionDocument(
        String description,
        String language
) {
}
