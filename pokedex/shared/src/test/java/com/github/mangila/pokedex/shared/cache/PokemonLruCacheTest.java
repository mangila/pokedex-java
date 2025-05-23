package com.github.mangila.pokedex.shared.cache;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonLruCacheTest {

    @Test
    void shouldEvictEntryWhenCacheIsFull() {
        var cache = new PokemonLruCache(3);
        cache.put("1", new Pokemon(1, "bulba"));
        cache.put("2", new Pokemon(2, "charmander"));
        cache.put("3", new Pokemon(3, "squirtle"));
        cache.put("4", new Pokemon(4, "pikachu"));
        assertThat(cache.get("1")).isNull();
    }

}