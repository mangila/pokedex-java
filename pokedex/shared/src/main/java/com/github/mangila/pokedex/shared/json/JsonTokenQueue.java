package com.github.mangila.pokedex.shared.json;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public record JsonTokenQueue(Queue<JsonToken> queue) {

    public static final JsonTokenQueue EMPTY = new JsonTokenQueue(new ArrayDeque<>(0));

    public void add(JsonToken token) {
        queue.add(token);
    }

    public @Nullable JsonToken poll() {
        return queue.poll();
    }

    public @Nullable JsonToken peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public JsonToken expect(JsonType type) {
        JsonToken token = poll();
        if (token == null || token.type() != type) {
            throw new NotValidJsonException("Expected type %s not found".formatted(type));
        }
        return token;
    }
}
