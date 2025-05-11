package com.github.mangila.pokedex.shared.json;

public record JsonParserConfig(int maxDepth) {
    public JsonParserConfig {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be greater than 0");
        }
    }
}
