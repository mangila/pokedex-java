package com.github.mangila.pokedex.backstage.shared.util;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.pokemonspecies.PokemonDescription;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JavaStreamUtilTest {

    @Test
    void testDistinctByKey() {
        var descriptions = Stream.of(
                        PokemonDescription.newBuilder()
                                .setDescription("Drifts in shallow seas. Anglers who hook them by accident are often punished by its stinging acid.")
                                .setLanguage("en")
                                .build(),
                        PokemonDescription.newBuilder()
                                .setDescription("Found in fields and mountains. Mistaking them for boulders, people often step or trip on them.")
                                .setLanguage("se")
                                .build(),
                        PokemonDescription.newBuilder()
                                .setDescription("hello world")
                                .setLanguage("en")
                                .build(),
                        PokemonDescription.newBuilder()
                                .setDescription("hello world")
                                .setLanguage("se")
                                .build()
                )
                .filter(JavaStreamUtil.distinctByKey(PokemonDescription::getLanguage));
        assertThat(descriptions).hasSize(2);
    }
}