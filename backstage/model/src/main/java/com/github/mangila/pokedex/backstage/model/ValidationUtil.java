package com.github.mangila.pokedex.backstage.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

final class ValidationUtil {

    private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Validator getValidator() {
        return VALIDATOR;
    }

}
