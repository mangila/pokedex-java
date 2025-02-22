package com.github.mangila.pokedex.backstage.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpeciesResponse(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("names") Names[] names,
        @JsonProperty("genera") Genera[] genera,
        @JsonProperty("evolution_chain") EvolutionChain evolutionChain,
        @JsonProperty("flavor_text_entries") FlavorTextEntries[] flavorTextEntries,
        @JsonProperty("varieties") Varieties[] varieties,
        @JsonProperty("is_baby") boolean baby,
        @JsonProperty("is_legendary") boolean legendary,
        @JsonProperty("is_mythical") boolean mythical
) {
}

