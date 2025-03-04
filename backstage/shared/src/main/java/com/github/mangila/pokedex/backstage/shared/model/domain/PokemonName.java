package com.github.mangila.pokedex.backstage.shared.model.domain;

import com.github.mangila.pokedex.backstage.shared.util.ValidationUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PokemonName {
    @NotNull(message = "Name cannot be null")
    @NotEmpty(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Name not valid string")
    private final String value;

    private PokemonName(String value) {
        this.value = value;
        ValidationUtil.validate(this);
    }

    public static PokemonName create(String name) {
        return new PokemonName(name);
    }

    public String getValue() {
        return value;
    }
}