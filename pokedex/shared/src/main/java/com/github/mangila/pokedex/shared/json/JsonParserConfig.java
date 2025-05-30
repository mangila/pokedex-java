package com.github.mangila.pokedex.shared.json;

/**
 * Configuration for the JsonParser.
 * Contains settings that control the behavior of the parser.
 */
public record JsonParserConfig(
        int maxDepth
) {
    /**
     * Creates a new JsonParserConfig with validation.
     * 
     * @param maxDepth the maximum depth of JSON nesting allowed
     * @throws IllegalArgumentException if maxDepth is less than or equal to 0
     */
    public JsonParserConfig {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be greater than 0");
        }
    }
}