package com.github.mangila.pokedex.scheduler.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@lombok.Builder
public record SpeciesResponse(
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
}
