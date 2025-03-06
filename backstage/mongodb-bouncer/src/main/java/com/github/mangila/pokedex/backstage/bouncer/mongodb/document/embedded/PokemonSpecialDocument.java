package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonSpecialPrototype;
import org.springframework.data.mongodb.core.mapping.Field;

public record PokemonSpecialDocument(
        @Field("is_special")
        boolean isSpecial,
        boolean legendary,
        boolean mythical,
        boolean baby
) {
    public PokemonSpecialPrototype toProto() {
        return PokemonSpecialPrototype.newBuilder()
                .setIsSpecial(isSpecial)
                .setLegendary(legendary)
                .setMythical(mythical)
                .setBaby(baby)
                .build();
    }

    public static PokemonSpecialDocument fromProto(PokemonSpecialPrototype proto) {
        return new PokemonSpecialDocument(
                proto.getIsSpecial(),
                proto.getLegendary(),
                proto.getMythical(),
                proto.getBaby()
        );
    }
}
