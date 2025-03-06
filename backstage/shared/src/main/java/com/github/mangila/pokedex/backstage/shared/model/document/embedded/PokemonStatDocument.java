package com.github.mangila.pokedex.backstage.shared.model.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonStatPrototype;

public record PokemonStatDocument(
        String name,
        int value
) {
    public PokemonStatPrototype toProto() {
        return PokemonStatPrototype.newBuilder()
                .setName(name)
                .setValue(value)
                .build();
    }

    public static PokemonStatDocument fromProto(PokemonStatPrototype proto) {
        return new PokemonStatDocument(
                proto.getName(),
                proto.getValue()
        );
    }
}
