package com.github.mangila.pokedex.shared.json.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public record JsonValue(Object value) {

    public String getString() {
        return (String) value;
    }

    public JsonObject getObject() {
        return (JsonObject) value;
    }

    public JsonArray getArray() {
        return (JsonArray) value;
    }

    public Number getNumber() {
        return (Number) value;
    }

    public Boolean getBoolean() {
        return (Boolean) value;
    }

    public boolean isNull() {
        return value == null;
    }
}
