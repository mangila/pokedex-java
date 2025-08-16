package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.util.Ensure;


public record JsonParserConfig(int maxDepth) {

    public static final int DEFAULT_MAX_DEPTH = 64;
    public static final JsonParserConfig DEFAULT = new JsonParserConfig(DEFAULT_MAX_DEPTH);

    public JsonParserConfig {
        Ensure.min(1, maxDepth);
    }
}