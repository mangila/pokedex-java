package com.github.mangila.pokedex.backstage.model.config;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

class ProtobufRuntimeHintsTest {

    @Test
    void shouldRegisterHints() {
        var hints = new RuntimeHints();
        new ProtobufRuntimeHints()
                .registerHints(hints, getClass().getClassLoader());
    }

}