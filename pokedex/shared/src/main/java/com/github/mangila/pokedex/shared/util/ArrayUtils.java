package com.github.mangila.pokedex.shared.util;

import java.util.Arrays;

public final class ArrayUtils {

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private ArrayUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void ensureArrayEquals(byte[] a, byte[] a2) {
        var equals = Arrays.equals(a, a2);
        if (!equals) {
            throw new IllegalStateException("Arrays are not equal");
        }
    }
}
