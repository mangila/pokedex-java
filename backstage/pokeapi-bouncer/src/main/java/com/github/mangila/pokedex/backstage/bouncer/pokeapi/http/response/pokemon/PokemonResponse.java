package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites.Sprites;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonResponsePrototype;

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
        @JsonProperty("types") List<Types> types
) {

    public PokemonResponsePrototype toProto() {
        return PokemonResponsePrototype.newBuilder()
                .setId(id)
                .setName(name)
                .setHeight(height)
                .setWeight(weight)
                .setIsDefault(isDefault)
                .setCries(cries.toProto())
                .setSprites(sprites.toProto())
                .addAllStats(stats.stream().map(Stats::toProto).toList())
                .addAllTypes(types.stream().map(Types::type).map(Type::name).toList())
                .build();
    }
}

