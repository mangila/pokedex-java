package com.github.mangila.pokedex.shared.json.model;

/**
 * Wrapper class for any JSON values.
 */
public record JsonValue(Object value) {

    public String unwrapString() {
        return (String) value;
    }

    public JsonObject unwrapObject() {
        return (JsonObject) value;
    }

    public JsonArray unwrapArray() {
        return (JsonArray) value;
    }

    public Number unwrapNumber() {
        return (Number) value;
    }

    public Boolean unwrapBoolean() {
        return (Boolean) value;
    }

    public boolean isNull() {
        return value == null;
    }
}
