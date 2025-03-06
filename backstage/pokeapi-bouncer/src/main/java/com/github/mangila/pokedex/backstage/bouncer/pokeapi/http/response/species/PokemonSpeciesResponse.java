package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonSpeciesResponsePrototype;

import java.util.List;

public record PokemonSpeciesResponse(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("generation") Generation generation,
        @JsonProperty("names") List<Names> names,
        @JsonProperty("genera") List<Genera> genera,
        @JsonProperty("evolution_chain") EvolutionChain evolutionChain,
        @JsonProperty("flavor_text_entries") List<FlavorTextEntries> flavorTextEntries,
        @JsonProperty("varieties") List<Varieties> varieties,
        @JsonProperty("is_baby") boolean baby,
        @JsonProperty("is_legendary") boolean legendary,
        @JsonProperty("is_mythical") boolean mythical
) {

    public PokemonSpeciesResponsePrototype toProto() {
        return PokemonSpeciesResponsePrototype.newBuilder()
                .setId(id)
                .setName(name)
                .setGeneration(generation.name())
                .addAllNames(names.stream().map(Names::toProto).toList())
                .addAllGenera(genera.stream().map(Genera::toProto).toList())
                .setEvolutionChainUrl(evolutionChain.url())
                .addAllFlavorTextEntries(flavorTextEntries.stream().map(FlavorTextEntries::toProto).toList())
                .addAllVarieties(varieties.stream().map(Varieties::pokemon).map(Pokemon::name).toList())
                .setIsBaby(baby)
                .setIsLegendary(legendary)
                .setIsMythical(mythical)
                .build();
    }

}

