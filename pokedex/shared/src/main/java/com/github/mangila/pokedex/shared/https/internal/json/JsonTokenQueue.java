package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.Queue;

public class JsonTokenQueue {

    private final Queue<JsonToken> queue;

    public JsonTokenQueue(Queue<JsonToken> queue) {
        this.queue = queue;
    }

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
