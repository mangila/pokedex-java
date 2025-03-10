package com.github.mangila.pokedex.backstage.shared.model.domain;


import java.util.EnumSet;

public enum Generation {
    GENERATION_I("generation-i"),
    GENERATION_II("generation-ii"),
    GENERATION_III("generation-iii"),
    GENERATION_IV("generation-iv"),
    GENERATION_V("generation-v"),
    GENERATION_VI("generation-vi"),
    GENERATION_VII("generation-vii"),
    GENERATION_VIII("generation-viii"),
    GENERATION_IX("generation-ix");

    private final String name;

    Generation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Generation from(String name) {
        return EnumSet.allOf(Generation.class).stream()
                .filter(generation -> generation.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with name: " + name));
    }
}