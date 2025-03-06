package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.NamePrototype;

public record Names(
        @JsonProperty("language") Language language,
        @JsonProperty("name") String name
) {
    public NamePrototype toProto() {
        return NamePrototype.newBuilder()
                .setName(name)
                .setLanguage(language().name())
                .build();
    }
}
