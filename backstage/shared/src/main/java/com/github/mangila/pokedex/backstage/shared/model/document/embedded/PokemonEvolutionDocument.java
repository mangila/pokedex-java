package com.github.mangila.pokedex.backstage.shared.model.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonEvolutionPrototype;

public record PokemonEvolutionDocument(
        int order,
        String name
) {
    public PokemonEvolutionPrototype toProto() {
        return PokemonEvolutionPrototype.newBuilder()
                .setOrder(order)
                .setName(name)
                .build();
    }

    public static PokemonEvolutionDocument fromProto(PokemonEvolutionPrototype proto) {
        return new PokemonEvolutionDocument(
                proto.getOrder(),
                proto.getName()
        );
    }
}
