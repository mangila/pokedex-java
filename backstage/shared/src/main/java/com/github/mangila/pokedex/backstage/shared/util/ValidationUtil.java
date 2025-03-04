package com.github.mangila.pokedex.backstage.shared.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public final class ValidationUtil {

    private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validate the Jakarta annotations
     */
    public static <T> void validate(T toValidate) {
        var violations = ValidationUtil.getValidator().validate(toValidate);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (var violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            throw new IllegalArgumentException("Validation failed: " + sb);
        }
    }

    private static Validator getValidator() {
        return VALIDATOR;
    }

}
