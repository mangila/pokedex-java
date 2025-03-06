package com.github.mangila.pokedex.backstage.bouncer.mongodb.document;

import com.github.mangila.pokedex.backstage.bouncer.mongodb.document.embedded.*;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.PokemonSpeciesPrototype;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("pokemon-species")
public record PokemonSpeciesDocument(
        @NotNull @Id int id,
        @NotNull @Field("name") String name,
        @NotNull @Field("generation") String generation,
        @NotNull @Field("names") List<PokemonNameDocument> names,
        @NotNull @Field("descriptions") List<PokemonDescriptionDocument> descriptions,
        @NotNull @Field("genera") List<PokemonGeneraDocument> genera,
        @NotNull @Field("evolutions") List<PokemonEvolutionDocument> evolutions,
        @NotNull @Field("varieties") List<PokemonDocument> varieties,
        @NotNull @Field("special") PokemonSpecialDocument special
) {

    public static PokemonSpeciesDocument fromProto(PokemonSpeciesPrototype proto) {
        return new PokemonSpeciesDocument(
                proto.getId(),
                proto.getName(),
                proto.getGeneration(),
                proto.getNamesList().stream().map(PokemonNameDocument::fromProto).toList(),
                proto.getDescriptionsList().stream().map(PokemonDescriptionDocument::fromProto).toList(),
                proto.getGeneraList().stream().map(PokemonGeneraDocument::fromProto).toList(),
                proto.getEvolutionsList().stream().map(PokemonEvolutionDocument::fromProto).toList(),
                proto.getVarietiesList().stream().map(PokemonDocument::fromProto).toList(),
                PokemonSpecialDocument.fromProto(proto.getSpecial())
        );
    }

    public PokemonSpeciesPrototype toProto() {
        return PokemonSpeciesPrototype.newBuilder()
                .setId(id)
                .setName(name)
                .setGeneration(generation)
                .addAllNames(names.stream().map(PokemonNameDocument::toProto).toList())
                .addAllGenera(genera.stream().map(PokemonGeneraDocument::toProto).toList())
                .addAllDescriptions(descriptions.stream().map(PokemonDescriptionDocument::toProto).toList())
                .addAllEvolutions(evolutions.stream().map(PokemonEvolutionDocument::toProto).toList())
                .addAllVarieties(varieties.stream().map(PokemonDocument::toProto).toList())
                .setSpecial(special.toProto())
                .build();
    }
}

