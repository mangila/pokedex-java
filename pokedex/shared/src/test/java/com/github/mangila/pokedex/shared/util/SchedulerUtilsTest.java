package com.github.mangila.pokedex.shared.util;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;

class SchedulerUtilsTest {

    @Test
    void testCreateFileNameGeneratesCorrectFileName() {
        URI uri = URI.create("http://example.com/images/image.png");
        String fileName = SchedulerUtils.createFileName("example", "123", uri);
        assertThat(fileName).isEqualTo("example-123.png");
    }

    @Test
    void testCreateFileNameThrowsNullPointerExceptionForNull() {
        assertThatThrownBy(() -> SchedulerUtils.createFileName("example", "123", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testCreateFileNameWithUnsupportedExtension() {
        URI uri = URI.create("http://example.com/files/document.txt");
        String fileName = SchedulerUtils.createFileName("doc", "001", uri);
        assertThat(fileName).isEqualTo("doc-001.txt");
    }

    @Test
    void testEnsureUriFromPokeApiWithValidRawGitHubUri() {
        URI uri = URI.create("https://raw.githubusercontent.com/some/repo/file.png");
        assertThatNoException().isThrownBy(() -> SchedulerUtils.ensureUriFromPokeApi(uri));
    }

    @Test
    void testEnsureUriFromPokeApiWithValidPokeApiUri() {
        URI uri = URI.create("https://pokeapi.co/api/v2/pokemon/ditto");
        assertThatNoException().isThrownBy(() -> SchedulerUtils.ensureUriFromPokeApi(uri));
    }

    @Test
    void testEnsureUriFromPokeApiThrowsExceptionForInvalidHost() {
        URI uri = URI.create("https://example.com/api/v2/pokemon/ditto");
        assertThatThrownBy(() -> SchedulerUtils.ensureUriFromPokeApi(uri))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEnsureUriFromPokeApiThrowsExceptionForNullUri() {
        assertThatThrownBy(() -> SchedulerUtils.ensureUriFromPokeApi(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

}