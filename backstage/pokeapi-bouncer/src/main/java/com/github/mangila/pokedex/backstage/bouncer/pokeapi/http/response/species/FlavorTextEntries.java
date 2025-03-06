package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.species;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.FlavorTextEntriesPrototype;

public record FlavorTextEntries(
        @JsonProperty("flavor_text") String flavorText,
        @JsonProperty("language") Language language
) {
    public FlavorTextEntriesPrototype toProto() {
        return FlavorTextEntriesPrototype.newBuilder()
                .setFlavorText(flavorText)
                .setLanguage(language.name())
                .build();
    }
}
