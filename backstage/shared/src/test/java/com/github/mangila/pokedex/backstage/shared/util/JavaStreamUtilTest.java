package com.github.mangila.pokedex.backstage.shared.util;

import com.github.mangila.pokedex.backstage.shared.model.document.embedded.PokemonDescriptionDocument;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JavaStreamUtilTest {

    @Test
    void testDistinctByKey() {
        var descriptions = Stream.of(
                        new PokemonDescriptionDocument("Found in fields and mountains. Mistaking them for boulders, people often step or trip on them.", "en"),
                        new PokemonDescriptionDocument("Drifts in shallow seas. Anglers who hook them by accident are often punished by its stinging acid.", "en"),
                        new PokemonDescriptionDocument("Hello world", "se"),
                        new PokemonDescriptionDocument("Asdf", "se")
                ).filter(JavaStreamUtil.distinctByKey(PokemonDescriptionDocument::language))
                .toList();
        assertThat(descriptions).hasSize(2);
    }
}