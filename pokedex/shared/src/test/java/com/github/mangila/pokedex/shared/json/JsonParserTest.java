package com.github.mangila.pokedex.shared.json;

import org.junit.jupiter.api.Test;

class JsonParserTest {

    @Test
    void parseTree() {
        String json = """
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
        var jsonParser = new JsonParser(10);
        var tree = jsonParser.parseTree(json);
        System.out.println(tree);
    }
}