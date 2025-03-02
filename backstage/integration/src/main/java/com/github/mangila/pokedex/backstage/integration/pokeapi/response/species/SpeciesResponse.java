package com.github.mangila.pokedex.backstage.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

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

    public String toJson(ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

