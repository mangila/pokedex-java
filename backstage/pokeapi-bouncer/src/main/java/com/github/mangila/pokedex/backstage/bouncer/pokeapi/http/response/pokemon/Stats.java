package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.StatsPrototype;

public record Stats(
        @JsonProperty("base_stat") int baseStat,
        @JsonProperty("stat") Stat stat
) {
    public StatsPrototype toProto() {
        return StatsPrototype.newBuilder()
                .setName(stat.name())
                .setValue(baseStat)
                .build();
    }
}
