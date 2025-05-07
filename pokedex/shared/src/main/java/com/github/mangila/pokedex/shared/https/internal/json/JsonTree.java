package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.Map;

public class JsonTree {

    private final Map<String, Object> tree = new java.util.HashMap<>();

    public void add(String key, Object value) {
        tree.put(key, value);
    }

    public Map<String, Object> getTree() {
        return tree;
    }
}
