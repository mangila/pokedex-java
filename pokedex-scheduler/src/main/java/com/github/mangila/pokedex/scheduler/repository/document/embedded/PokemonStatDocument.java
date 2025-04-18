package com.github.mangila.pokedex.scheduler.repository.document.embedded;

import com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.Stats;

import java.util.List;

@lombok.Builder
public record PokemonStatDocument(
        String name,
        int value
) {

    public static PokemonStatDocument of(Stats stats) {
        return PokemonStatDocument.builder()
                .name(stats.stat().name())
                .value(stats.baseStat())
                .build();
    }

    public static List<PokemonStatDocument> of(List<Stats> stats) {
        var totalSum = stats.stream()
                .mapToInt(Stats::baseStat)
                .sum();
        var total = PokemonStatDocument.builder()
                .name("total")
                .value(totalSum)
                .build();
        var list = new java.util.ArrayList<>(stats.stream()
                .map(PokemonStatDocument::of)
                .toList());
        list.add(total);
        return list;
    }
}
