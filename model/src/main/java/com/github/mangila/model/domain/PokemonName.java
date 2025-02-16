package com.github.mangila.model.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@lombok.Getter
@lombok.NoArgsConstructor
public class PokemonName implements Serializable {
    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Name not valid string")
    String name;

    public PokemonName(String name) {
        this.name = name;
        validate();
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
                for (ConstraintViolation<PokemonName> violation : violations) {
                    sb.append(violation.getMessage()).append("\n");
                }
                throw new IllegalArgumentException("Validation failed: " + sb);
            }
        }
    }
}
