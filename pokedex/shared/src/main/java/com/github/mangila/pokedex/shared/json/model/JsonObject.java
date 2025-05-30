package com.github.mangila.pokedex.shared.json.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonObject {

    public static final JsonObject EMPTY = new JsonObject();

    private final Map<String, JsonValue> tree = new LinkedHashMap<>();

    public void add(String key, JsonValue value) {
        tree.put(key, value);
    }

    public String getString(String key) {
        return tree.get(key)
                .getString();
    }

    public JsonObject getObject(String key) {
        return tree.get(key)
                .getObject();
    }

    public JsonArray getArray(String key) {
        return tree.get(key)
                .getArray();
    }

    public Number getNumber(String key) {
        return tree.get(key)
                .getNumber();
    }

    public Boolean getBoolean(String key) {
        return tree.get(key)
                .getBoolean();
    }

    public boolean isNull(String key) {
        return tree.get(key)
                .isNull();
    }

    public JsonValue get(String key) {
        return tree.get(key);
    }
}
