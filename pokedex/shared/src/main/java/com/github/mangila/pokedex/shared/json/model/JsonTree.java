package com.github.mangila.pokedex.shared.json.model;

import java.util.HashMap;
import java.util.Map;

public class JsonTree {

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
        return tree.get(key).getString();
    }

    public Number getNumber(String key) {
        return tree.get(key).getNumber();
    }

    public Boolean getBoolean(String key) {
        return tree.get(key).getBoolean();
    }

    public boolean isNull(String key) {
        return tree.get(key).isNull();
    }

    public int size() {
        return tree.size();
    }
}
