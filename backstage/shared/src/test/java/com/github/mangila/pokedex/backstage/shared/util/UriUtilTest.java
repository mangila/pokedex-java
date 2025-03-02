package com.github.mangila.pokedex.backstage.shared.util;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class UriUtilTest {

    @Test
    void testGetLastPathSegment() {
        var lastPathSegment = UriUtil.getLastPathSegment(URI.create("https://www.xyz.com/hello/world"));
        assertThat(lastPathSegment).isEqualTo("world");
    }

    @Test
    void testGetLastPathSegmentExtraForwardSlash() {
        var lastPathSegment = UriUtil.getLastPathSegment(URI.create("https://www.xyz.com/hello/world/"));
        assertThat(lastPathSegment).isEqualTo("world");
    }

    @Test
    void testQueryParam() {
        var lastPathSegment = UriUtil.getLastPathSegment(URI.create("https://www.xyz.com/hello/world?queryParam=value"));
        assertThat(lastPathSegment).isEqualTo("world");
    }
}