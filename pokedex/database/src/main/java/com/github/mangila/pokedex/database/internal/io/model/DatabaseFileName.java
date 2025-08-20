package com.github.mangila.pokedex.database.internal.io.model;

import com.github.mangila.pokedex.shared.util.Ensure;

/**
 * Represents a validated database file name with a predefined suffix requirement.
 * This class ensures that the provided file name is not null, not blank, and ends
 * with the required file suffix.
 * <p>
 * Validation rules include:
 * - The input file name must not be null.
 * - The input file name must not be blank.
 * - The input file name must end with the predefined suffix ".yakvs".
 * <p>
 * The suffix requirement is enforced using the {@code YAKVS_SUFFIX} constant.
 * If the provided file name does not meet these criteria, an {@code IllegalArgumentException}
 * is thrown during instantiation.
 */
public record DatabaseFileName(String value) {

    public static final String YAKVS_SUFFIX = ".yakvs";

    public DatabaseFileName {
        Ensure.notNull(value, "database name must not be null");
        Ensure.notBlank(value, "database name must not be blank");
        if (!value.endsWith(YAKVS_SUFFIX)) {
            throw new IllegalArgumentException("database value must have suffix: %s".formatted(YAKVS_SUFFIX));
        }
    }

}
