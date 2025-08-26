package com.github.mangila.pokedex.shared.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public final class Ensure {

    private Ensure() {
        throw new IllegalStateException("Utility class");
    }

    public static void notNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
    }

    public static void notNull(Object object, Class<?> clazz) {
        if (object == null) {
            throw new IllegalArgumentException(clazz.getName() + " cannot be null");
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEquals(int first, int second, Supplier<RuntimeException> ex) {
        if (first == second) {
            throw ex.get();
        }
    }

    public static void notEquals(int first, int second, String message) {
        if (first != second) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void equals(byte[] a, byte[] a2) {
        boolean equals = Arrays.equals(a, a2);
        if (!equals) {
            throw new IllegalArgumentException("Arrays are not equal");
        }
    }

    public static void equals(byte[] a, byte[] a2, String message) {
        boolean equals = Arrays.equals(a, a2);
        if (!equals) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, Supplier<RuntimeException> ex) {
        if (object == null) {
            throw ex.get();
        }
    }

    public static void notEmpty(Object[] array, Class<?> clazz) {
        notNull(array, clazz);
        if (array.length == 0) {
            throw new IllegalArgumentException("%s cannot be empty".formatted(clazz.getSimpleName()));
        }
    }


    public static void min(int min, int target) {
        if (target < min) {
            throw new IllegalArgumentException("Target must be greater than or equal to %d but was %d"
                    .formatted(min, target)
            );
        }
    }

    public static void min(long min, long target) {
        if (target < min) {
            throw new IllegalArgumentException("Target must be greater than or equal to %d but was %d"
                    .formatted(min, target)
            );
        }
    }

    public static void max(int max, int target) {
        if (target > max) {
            throw new IllegalArgumentException("Target must be less than or equal to %d but was %d"
                    .formatted(max, target)
            );
        }
    }

    public static void equals(Object object, Object another) {
        if (!Objects.equals(object, another)) {
            throw new IllegalArgumentException("Objects are not equal: %s != %s".formatted(object, another));
        }
    }

    public static void notBlank(String value, String message) {
        notNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
