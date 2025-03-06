package com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonPrototype;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

public record PokemonDocument(
        @Field("pokemon_id")
        int id,
        String name,
        @Field("is_default")
        boolean isDefault,
        String height,
        String weight,
        List<PokemonTypeDocument> types,
        List<PokemonStatDocument> stats,
        List<PokemonMediaDocument> media
) {

    public PokemonPrototype toProto() {
        var typeProtos = types.stream()
                .map(PokemonTypeDocument::toProto)
                .toList();
        var statProtos = stats.stream()
                .map(PokemonStatDocument::toProto)
                .toList();
        // TODO add media protos
        return PokemonPrototype.newBuilder()
                .setId(id)
                .setName(name)
                .setIsDefault(isDefault)
                .setHeight(height)
                .setWeight(weight)
                .addAllTypes(typeProtos)
                .addAllStats(statProtos)
                .build();
    }

    public static PokemonDocument fromProto(PokemonPrototype proto) {
        return new PokemonDocument(
                proto.getId(),
                proto.getName(),
                proto.getIsDefault(),
                proto.getHeight(),
                proto.getWeight(),
                proto.getTypesList().stream().map(PokemonTypeDocument::fromProto).toList(),
                proto.getStatsList().stream().map(PokemonStatDocument::fromProto).toList(),
                proto.getMediaList().stream().map(PokemonMediaDocument::fromProto).toList()
        );
    }

}
