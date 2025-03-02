package com.github.mangila.pokedex.backstage.shared.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public final class ValidationUtil {

    private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Validator getValidator() {
        return VALIDATOR;
    }

}
