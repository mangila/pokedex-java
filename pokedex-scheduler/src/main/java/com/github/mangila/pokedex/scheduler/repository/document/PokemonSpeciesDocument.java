package com.github.mangila.pokedex.scheduler.repository.document;

import com.github.mangila.pokedex.scheduler.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.scheduler.pokeapi.response.species.SpeciesResponse;
import com.github.mangila.pokedex.scheduler.repository.document.embedded.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("pokemon")
@lombok.Builder
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

    public static PokemonSpeciesDocument of(SpeciesResponse speciesResponse,
                                            EvolutionChainResponse evolutionChainResponse,
                                            List<PokemonResponse> varieties) {
        return PokemonSpeciesDocument.builder()
                .id(speciesResponse.id())
                .name(speciesResponse.name())
                .generation(speciesResponse.generation().name())
                .names(PokemonNameDocument.of(speciesResponse.names()))
                .descriptions(PokemonDescriptionDocument.of(speciesResponse.flavorTextEntries()))
                .genera(PokemonGeneraDocument.of(speciesResponse.genera()))
                .evolutions(PokemonEvolutionDocument.of(evolutionChainResponse))
                .varieties(PokemonDocument.of(varieties))
                .special(PokemonSpecialDocument.builder()
                        .isSpecial(speciesResponse.baby() && speciesResponse.legendary() && speciesResponse.mythical())
                        .baby(speciesResponse.baby())
                        .legendary(speciesResponse.legendary())
                        .mythical(speciesResponse.mythical())
                        .build())
                .build();
    }

}

