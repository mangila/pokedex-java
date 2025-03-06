package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GeneraPrototype;

public record Genera(
        @JsonProperty("language") Language language,
        @JsonProperty("genus") String genus
) {
    public GeneraPrototype toProto() {
        return GeneraPrototype.newBuilder()
                .setGenus(genus)
                .setLanguage(language.name())
                .build();
    }
}
