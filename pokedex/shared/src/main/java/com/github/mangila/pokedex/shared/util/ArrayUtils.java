package com.github.mangila.pokedex.shared.util;

public final class ArrayUtils {

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private ArrayUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isEmpty(byte[] a) {
        return a.length == 0;
    }

    public static boolean isEmptyOrNull(byte[] a) {
        return a == null || isEmpty(a);
    }
}
