package com.github.mangila.pokedex.shared.json;

import java.util.Queue;

public record JsonTokenQueue(Queue<JsonToken> queue) {

    public void add(JsonToken token) {
        queue.add(token);
    }

    public JsonToken poll() {
        return queue.poll();
    }

    public JsonToken peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public JsonToken expect(JsonType type) {
        var token = poll();
        if (token == null || token.type() != type) {
            throw new InvalidJsonException(InvalidJsonException.PARSE_ERROR_MESSAGE);
        }
        return token;
    }
}
