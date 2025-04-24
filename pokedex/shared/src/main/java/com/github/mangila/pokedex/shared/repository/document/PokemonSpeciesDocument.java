package com.github.mangila.pokedex.shared.repository.document;

import com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.shared.pokeapi.response.species.SpeciesResponse;
import com.github.mangila.pokedex.shared.repository.document.embedded.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document("pokemon")
public record PokemonSpeciesDocument(
        @NotNull @Id int id,
        @NotNull @Field("name") String name,
        @NotNull @Field("generation") String generation,
        @NotNull @Field("names") List<PokemonNameDocument> names,
        @NotNull @Field("descriptions") List<PokemonDescriptionDocument> descriptions,
        @NotNull @Field("genera") List<PokemonGeneraDocument> genera,
        @NotNull @Field("evolutions") List<PokemonEvolutionDocument> evolutions,
        @NotNull @Field("varieties") List<PokemonDocument> varieties,
        @NotNull @Field("special") PokemonSpecialDocument special,
        @LastModifiedBy String lastModifiedBy,
        @LastModifiedDate Instant lastModifiedDate
) {

    public static PokemonSpeciesDocument fromSpeciesEvolutionAndVarieties(SpeciesResponse speciesResponse,
                                            EvolutionChainResponse evolutionChainResponse,
                                            List<PokemonResponse> varieties) {
        return new PokemonSpeciesDocument(
                speciesResponse.id(),
                speciesResponse.name(),
                speciesResponse.generation().name(),
                PokemonNameDocument.fromNamesList(speciesResponse.names()),
                PokemonDescriptionDocument.fromFlavorTextEntries(speciesResponse.flavorTextEntries()),
                PokemonGeneraDocument.fromGeneraList(speciesResponse.genera()),
                PokemonEvolutionDocument.fromEvolutionChainResponse(evolutionChainResponse),
                PokemonDocument.fromPokemonResponses(varieties),
                new PokemonSpecialDocument(
                        speciesResponse.baby() && speciesResponse.legendary() && speciesResponse.mythical(),
                        speciesResponse.legendary(),
                        speciesResponse.mythical(),
                        speciesResponse.baby()
                ),
                null,
                null
        );
    }

}
