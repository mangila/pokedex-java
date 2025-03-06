package com.github.mangila.pokedex.backstage.shared.model.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonGeneraPrototype;

public record PokemonGeneraDocument(
        String genera,
        String language
) {

    public PokemonGeneraPrototype toProto() {
        return PokemonGeneraPrototype.newBuilder()
                .setGenera(genera)
                .setLanguage(language)
                .build();
    }

    public static PokemonGeneraDocument fromProto(PokemonGeneraPrototype proto) {
        return new PokemonGeneraDocument(
                proto.getGenera(),
                proto.getLanguage()
        );
    }
}