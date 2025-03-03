package com.github.mangila.pokedex.backstage.shared.model.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.shared.model.document.embedded.*;
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
    public String toJson(ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

