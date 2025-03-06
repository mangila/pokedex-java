package com.github.mangila.pokedex.backstage.shared.model.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonNamePrototype;

public record PokemonNameDocument(
        String name,
        String language
) {

    public PokemonNamePrototype toProto() {
        return PokemonNamePrototype.newBuilder()
                .setName(name)
                .setLanguage(language)
                .build();
    }

    public static PokemonNameDocument fromProto(PokemonNamePrototype proto) {
        return new PokemonNameDocument(
                proto.getName(),
                proto.getLanguage()
        );
    }
}
