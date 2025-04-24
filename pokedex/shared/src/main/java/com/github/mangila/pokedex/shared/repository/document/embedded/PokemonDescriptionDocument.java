package com.github.mangila.pokedex.shared.repository.document.embedded;


import com.github.mangila.pokedex.shared.pokeapi.response.species.FlavorTextEntries;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record PokemonDescriptionDocument(
        String description,
        String language
) {

    private static final Matcher REPLACE_LINE_BREAKS = Pattern.compile("[\r\n\t\f]+").matcher("");

    public static PokemonDescriptionDocument fromFlavorTextEntry(FlavorTextEntries flavorTextEntries) {
        return new PokemonDescriptionDocument(
                REPLACE_LINE_BREAKS.reset(flavorTextEntries.flavorText()).replaceAll(" "),
                flavorTextEntries.language().name()
        );
    }

    public static List<PokemonDescriptionDocument> fromFlavorTextEntries(List<FlavorTextEntries> flavorTextEntries) {
        return flavorTextEntries.stream()
                .map(PokemonDescriptionDocument::fromFlavorTextEntry)
                .toList();
    }
}
