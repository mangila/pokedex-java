package com.github.mangila.pokedex.database.wal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalWriteBufferTest {

    @Test
    void get() {
        WalWriteBuffer buffer = new WalWriteBuffer();
        assertThat(buffer.get(0).isEmpty()).isTrue();
        assertThat(buffer.get(1024).isEmpty()).isFalse();
        assertThat(buffer.get(5523).isEmpty()).isFalse();
        assertThat(buffer.get(WalWriteBuffer.DEFAULT_BUFFER_SIZE + 1).isEmpty())
                .isTrue();
        assertThatThrownBy(() -> buffer.get(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}