package com.github.mangila.pokedex.shared.json.model;

import java.util.ArrayList;
import java.util.List;

public class JsonArray {

    private final List<JsonValue> list = new ArrayList<>();

    public void add(JsonValue value) {
        list.add(value);
    }

    public List<JsonValue> values() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public JsonValue get(int index) {
        return list.get(index);
    }
}
