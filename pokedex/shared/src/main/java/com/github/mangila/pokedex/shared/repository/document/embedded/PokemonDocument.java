package com.github.mangila.pokedex.shared.repository.document.embedded;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.PokemonResponse;

import java.util.Collections;
import java.util.List;

@lombok.Builder
public record PokemonDocument(
        @Field("pokemon_id")
        @Indexed
        int id,
        String name,
        @Field("is_default")
        boolean isDefault,
        String height,
        String weight,
        List<PokemonTypeDocument> types,
        List<PokemonStatDocument> stats,
        List<PokemonMediaDocument> media
) {

    private static final int METER_DENOMINATOR = 10;
    private static final int KILOGRAM_DENOMINATOR = 10;

    public static PokemonDocument of(PokemonResponse response) {
        return PokemonDocument.builder()
                .id(response.id())
                .name(response.name())
                .isDefault(response.isDefault())
                .height(String.valueOf((double) response.height() / METER_DENOMINATOR))
                .weight(String.valueOf((double) response.weight() / KILOGRAM_DENOMINATOR))
                .types(PokemonTypeDocument.of(response.types()))
                .stats(PokemonStatDocument.of(response.stats()))
                .media(Collections.emptyList())
                .build();
    }

    public static List<PokemonDocument> of(List<PokemonResponse> varieties) {
        return varieties.stream().map(PokemonDocument::of).toList();
    }
}
