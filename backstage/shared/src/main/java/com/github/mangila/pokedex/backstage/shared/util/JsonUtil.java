package com.github.mangila.pokedex.backstage.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T of(String json, ObjectMapper objectMapper, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
