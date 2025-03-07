package com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

class SpritesTest {

    @Test
    void abc() {
        var r = new Reflections("com.github.mangila.pokedex.backstage.bouncer.pokeapi.http.response.pokemon.sprites");
        var sub = r.getSubTypesOf(Record.class);
        var l = sub.stream()
                .map(Class::getFields)
                .toList();
        System.out.println(l);
    }

}