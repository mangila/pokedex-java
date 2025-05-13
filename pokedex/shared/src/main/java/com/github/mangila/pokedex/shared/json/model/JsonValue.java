package com.github.mangila.pokedex.shared.json.model;

public record JsonValue(Object value) {

    public String getString() {
        return (String) value;
    }

    public JsonObject getObject() {
        return (JsonObject) value;
    }

}
