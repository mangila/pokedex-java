package com.github.mangila.model.domain;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PokemonId(
        @NotNull(message = "Id cannot be null")
        @NotEmpty(message = "Id cannot be empty")
        @Size(min = 1, max = 5, message = "Id must be between 1 and 5 characters")
        @Pattern(regexp = "^[0-9]+$", message = "Id not valid number")
        String id
) {
    public PokemonId(String id) {
        this.id = id;
        validate();
    }

    public PokemonId(Integer id) {
        this(String.valueOf(id));
    }

    public Integer toInteger() {
        return Integer.valueOf(id);
    }

    /**
     * Validate the Jakarta annotations
     */
    private void validate() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var violations = validator.validate(this);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (var violation : violations) {
                    sb.append(violation.getMessage()).append("\n");
                }
                throw new IllegalArgumentException("Validation failed: " + sb);
            }
        }
    }
}
