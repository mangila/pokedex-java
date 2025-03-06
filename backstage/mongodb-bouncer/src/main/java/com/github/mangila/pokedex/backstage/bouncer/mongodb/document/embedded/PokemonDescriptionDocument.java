package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonDescriptionPrototype;

public record PokemonDescriptionDocument(
        String description,
        String language
) {

    public PokemonDescriptionPrototype toProto() {
        return PokemonDescriptionPrototype.newBuilder()
                .setDescription(description)
                .setLanguage(language)
                .build();
    }

    public static PokemonDescriptionDocument fromProto(PokemonDescriptionPrototype proto) {
        return new PokemonDescriptionDocument(
                proto.getDescription(),
                proto.getLanguage()
        );
    }
}
