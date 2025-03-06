package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonTypePrototype;

public record PokemonTypeDocument(
        String type
) {
    public PokemonTypePrototype toProto() {
        return PokemonTypePrototype.newBuilder().setType(type).build();
    }

    public static PokemonTypeDocument fromProto(PokemonTypePrototype proto) {
        return new PokemonTypeDocument(
                proto.getType()
        );
    }
}
