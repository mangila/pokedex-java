package com.github.mangila.pokedex.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArrayUtilsTest {

    @Test
    @DisplayName("Should verify empty byte array properties")
    void shouldVerifyEmptyByteArrayProperties() {
        assertThat(ArrayUtils.EMPTY_BYTE_ARRAY).isNotNull();
        assertThat(ArrayUtils.EMPTY_BYTE_ARRAY).isEmpty();
        assertThat(ArrayUtils.EMPTY_BYTE_ARRAY).hasSize(0);
    }

    @Test
    @DisplayName("Should not throw exception when arrays are equal")
    void shouldNotThrowExceptionWhenArraysAreEqual() {
        byte[] array1 = new byte[]{1, 2, 3};
        byte[] array2 = new byte[]{1, 2, 3};

        // Should not throw an exception
        ArrayUtils.ensureArrayEquals(array1, array2);
    }

    @Test
    @DisplayName("Should throw exception when arrays have different values")
    void shouldThrowExceptionWhenArraysHaveDifferentValues() {
        byte[] array1 = new byte[]{1, 2, 3};
        byte[] array2 = new byte[]{1, 2, 4};

        assertThatThrownBy(() -> ArrayUtils.ensureArrayEquals(array1, array2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Arrays are not equal");
    }
}
