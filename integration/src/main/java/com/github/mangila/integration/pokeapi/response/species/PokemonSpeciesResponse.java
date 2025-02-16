package com.github.mangila.integration.pokeapi.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PokemonSpeciesResponse(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("name")
        String name,
        @JsonProperty("is_baby")
        boolean baby,
        @JsonProperty("is_legendary")
        boolean legendary,
        @JsonProperty("is_mythical")
        boolean mythical,
        @JsonProperty("evolution_chain")
        EvolutionChain evolutionChain,
        @JsonProperty("generation")
        Generation generation,
        @JsonProperty("varieties")
        List<Variety> varieties
) {
}
