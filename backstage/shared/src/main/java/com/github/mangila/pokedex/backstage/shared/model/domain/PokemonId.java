package com.github.mangila.pokedex.backstage.shared.model.domain;

import com.github.mangila.pokedex.backstage.shared.util.ValidationUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PokemonId {
    @NotNull(message = "Id cannot be null")
    @NotEmpty(message = "Id cannot be empty")
    @Size(min = 1, max = 5, message = "Id must be between 1 and 5 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Id not valid number")
    private final String value;

    private PokemonId(String value) {
        this.value = value;
        ValidationUtil.validate(this);
    }

    public static PokemonId create(String id) {
        return new PokemonId(id);
    }

    public static PokemonId create(Integer id) {
        return new PokemonId(String.valueOf(id));
    }

    public String getValue() {
        return value;
    }

    public Integer getValueAsInteger() {
        return Integer.parseInt(value);
    }
}
