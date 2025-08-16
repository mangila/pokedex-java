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
                .unwrapString();
    }

    public JsonObject getObject(String key) {
        return tree.get(key)
                .unwrapObject();
    }

    public JsonArray getArray(String key) {
        return tree.get(key)
                .unwrapArray();
    }

    public Number getNumber(String key) {
        return tree.get(key)
                .unwrapNumber();
    }

    public Boolean getBoolean(String key) {
        return tree.get(key)
                .unwrapBoolean();
    }

    public boolean isNull(String key) {
        return tree.get(key)
                .isNull();
    }

    public JsonValue getValue(String key) {
        return tree.get(key);
    }
}
