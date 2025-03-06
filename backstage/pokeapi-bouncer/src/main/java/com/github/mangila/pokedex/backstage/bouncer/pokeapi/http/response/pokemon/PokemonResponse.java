package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites.Sprites;

import java.util.List;

public record PokemonResponse(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("height") int height,
        @JsonProperty("weight") int weight,
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("cries") Cries cries,
        @JsonProperty("sprites") Sprites sprites,
        @JsonProperty("stats") List<Stats> stats,
        @JsonProperty("types") List<Types> types,
        @JsonProperty("abilities") List<Abilities> abilities
) {
    public String toJson(ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

