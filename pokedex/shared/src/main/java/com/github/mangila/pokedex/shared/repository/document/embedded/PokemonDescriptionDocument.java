package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.species.FlavorTextEntries;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@lombok.Builder
public record PokemonDescriptionDocument(
        String description,
        String language
) {

    private static final Matcher REPLACE_LINE_BREAKS = Pattern.compile("[\r\n\t\f]+").matcher("");

    public static PokemonDescriptionDocument of(FlavorTextEntries flavorTextEntries) {
        return PokemonDescriptionDocument.builder()
                .description(REPLACE_LINE_BREAKS.reset(flavorTextEntries.flavorText()).replaceAll(" "))
                .language(flavorTextEntries.language().name())
                .build();
    }

    public static List<PokemonDescriptionDocument> of(List<FlavorTextEntries> flavorTextEntries) {
        return flavorTextEntries.stream()
                .map(PokemonDescriptionDocument::of)
                .toList();
    }
}
