package com.github.mangila.pokedex.shared.json.model;

import java.util.HashMap;
import java.util.Map;

public class JsonRoot {

    public static final JsonRoot EMPTY = new JsonRoot();
    private final Map<String, JsonValue> tree = new HashMap<>();

    public void add(String key, JsonValue value) {
        tree.put(key, value);
    }

    public JsonArray getArray(String key) {
        return (JsonArray) tree.get(key).value();
    }

    public JsonObject getObject(String key) {
        return (JsonObject) tree.get(key).value();
    }

    public JsonValue getValue(String key) {
        return tree.get(key);
    }

    public String getString(String key) {
        return tree.get(key).unwrapString();
    }

    public Number getNumber(String key) {
        return tree.get(key).unwrapNumber();
    }

    public Boolean getBoolean(String key) {
        return tree.get(key).unwrapBoolean();
    }

    public boolean isNull(String key) {
        return tree.get(key).isNull();
    }

    public int size() {
        return tree.size();
    }

    public boolean isEmpty() {
        return tree.isEmpty();
    }
}
