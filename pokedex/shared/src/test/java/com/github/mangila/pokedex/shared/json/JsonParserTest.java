package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JsonParserTest {

    private final String complexJson = """
             {
                "escape-quotation-mark": "escape-\\"message\\"",
                "escape-reverse-solidus": "escape-\\\\message\\\\",
                "escape-solidus": "escape-\\/message\\/",
                "escape-backspace": "escape-\\bmessage\\b",
                "escape-formfeed": "escape-\\fmessage\\f",
                "escape-linefeed": "escape-\\nmessage\\n",
                "escape-carriage-return": "escape-\\rmessage\\r",
                "escape-horizontal-tab": "escape-\\tmessage\\t",
                "escape-unicode": "escape-\\u1234message\\u1234",
                "science-number": 1234e5,
                "programmer-number": 1.23E6,
                "negative-number": -1234,
                "escaped-formfeed-char": "hh\\f",
                "escaped-unicode": "\\u1234",
                "nested-object": {
                    "key": "value",
                    "number": 42
                },
                "nested-array": [1, 2, 3, 4],
                "mixed-array": ["text", 42, true, null],
                "object-array": [
                    { "id": 1, "name": "Alice", "active": true },
                    { "id": 2, "name": "Bob", "active": false },
                    { "id": 3, "name": "Charlie", "active": true }
                ],
                "deep-nested": {
                    "level1": {
                        "level2": {
                            "level3": {
                                "level4": {
                                    "level5": {
                                        "level6": {
                                            "level7": {
                                                "level8": {
                                                    "level9": {
                                                        "level10": {
                                                            "level11": "reached"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                "boolean-true": true,
                "boolean-false": false,
                "null-value": null
                }
            """;

    private final String simpleJson = """
            {
                "value": "pikachu",
                "type": "electric",
                "level": 25,
                "stats": {
                    "hp": 35,
                    "attack": 55,
                    "defense": 40
                },
                "moves": ["thunder shock", "quick attack", "thunderbolt"],
                "isStarter": false
            }
            """;

    @BeforeEach
    void setupParser() {
        // Reset and configure JsonParser with a default max depth of 64
        JsonParser.reset();
        JsonParser.configure(new JsonParserConfig(64));
    }

    @Test
    @DisplayName("Should parse simple json")
    void shouldParseSimpleJson() {
        var jsonParser = JsonParser.getInstance();
        var jsonTree = jsonParser.parseTree(simpleJson);

        assertThat(jsonTree).isNotNull();
        assertThat(jsonTree.getString("value")).isEqualTo("pikachu");
        assertThat(jsonTree.getString("type")).isEqualTo("electric");
        assertThat(jsonTree.getNumber("level").intValue()).isEqualTo(25);
        assertThat(jsonTree.getBoolean("isStarter")).isFalse();

        var stats = jsonTree.getObject("stats");
        assertThat(stats.getNumber("hp").intValue()).isEqualTo(35);
        assertThat(stats.getNumber("attack").intValue()).isEqualTo(55);
        assertThat(stats.getNumber("defense").intValue()).isEqualTo(40);

        var moves = jsonTree.getArray("moves");
        assertThat(moves.size()).isEqualTo(3);
        assertThat(moves.get(0).getString()).isEqualTo("thunder shock");
        assertThat(moves.get(1).getString()).isEqualTo("quick attack");
        assertThat(moves.get(2).getString()).isEqualTo("thunderbolt");
    }

    @Test
    @DisplayName("Should fail when json depth exceeds max depth")
    void shouldFailWhenJsonDepthExceedsMaxDepth() {
        // Reset and configure with lower max depth
        JsonParser.reset();
        JsonParser.configure(new JsonParserConfig(10));
        var jsonParser = JsonParser.getInstance();

        assertThatThrownBy(() -> jsonParser.parseTree(complexJson))
                .isInstanceOf(InvalidJsonException.class);
    }

    @Test
    @DisplayName("Should parse in parallel")
    void shouldParseInParallel() {
        var threads = VirtualThreadConfig.newFixedThreadPool(2);
        var jsonParser = JsonParser.getInstance();

        var t1 = threads.submit(() -> {
            String json = """
                    {
                        "value": "charmander",
                        "type": "fire",
                        "level": 7
                    }
                    """;
            return jsonParser.parseTree(json);
        });

        var t2 = threads.submit(() -> {
            String json = """
                    {
                        "value": "bulbasaur",
                        "type": "grass",
                        "level": 5,
                        "hp": 45,
                        "isStarter": true
                    }
                    """;
            return jsonParser.parseTree(json);
        });

        assertThatCode(() -> {
            JsonTree result1 = t1.get();
            JsonTree result2 = t2.get();

            assertThat(result1.getString("value")).isEqualTo("charmander");
            assertThat(result1.getString("type")).isEqualTo("fire");

            assertThat(result2.getString("value")).isEqualTo("bulbasaur");
            assertThat(result2.getString("type")).isEqualTo("grass");
            assertThat(result2.getBoolean("isStarter")).isTrue();
        }).doesNotThrowAnyException();

        threads.shutdown();
    }

    @Test
    @DisplayName("Should get configured max depth")
    void shouldGetConfiguredMaxDepth() {
        JsonParser.reset();
        JsonParser.configure(new JsonParserConfig(42));
        var jsonParser = JsonParser.getInstance();
        assertThat(jsonParser.getMaxDepth()).isEqualTo(42);
    }

    @Test
    @DisplayName("Should throw when configured twice")
    void shouldThrowWhenConfiguredTwice() {
        // First configuration is done in setupParser()
        assertThatThrownBy(() -> JsonParser.configure(new JsonParserConfig(100)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JsonParser is already configured");
    }

    @Nested
    @DisplayName("JsonParserConfig tests")
    class JsonParserConfigTest {

        @Test
        @DisplayName("Should throw when invalid max depth")
        void shouldThrowWhenInvalidMaxDepth() {
            assertThatThrownBy(() -> new JsonParserConfig(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxDepth must be greater than 0");

            assertThatThrownBy(() -> new JsonParserConfig(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxDepth must be greater than 0");
        }

        @Test
        @DisplayName("Should create config with valid max depth")
        void shouldCreateConfigWithValidMaxDepth() {
            // When
            var config = new JsonParserConfig(42);
            // Then
            assertThat(config.maxDepth()).isEqualTo(42);
        }
    }
}
