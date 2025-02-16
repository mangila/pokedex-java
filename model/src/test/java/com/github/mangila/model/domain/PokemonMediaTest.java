package com.github.mangila.model.domain;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonMediaTest {

    @Test
    void Test() throws MalformedURLException {
        var media = new PokemonMedia(
                new PokemonId(1),
                new PokemonName("bulbasaur"),
                "front-default",
                new URL("https://picture.com/pic.png")
        );
        assertThat(media.buildFileExtension())
                .isEqualTo("png");
        assertThat(media.createContentType())
                .isEqualTo("image/png");
        assertThat(media.buildFileName())
                .isEqualTo("bulbasaur-front-default.png");
    }

    @Test
    void fail() throws MalformedURLException {
        var media = new PokemonMedia(
                new PokemonId(1),
                new PokemonName("bulbasaur"),
                "front-default",
                new URL("https://picture.com/pic.xyz")
        );
        assertThatThrownBy(media::createContentType)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown extension: " + "xyz");
    }
}