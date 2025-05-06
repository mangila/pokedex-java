package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.ArrayDeque;
import java.util.Queue;

import static com.github.mangila.pokedex.shared.https.internal.json.InvalidJsonException.UNBALANCED_PARENTHESES_ERROR_MESSAGE;

public class JsonValidator {

    public static void ensureValidParentheses(Queue<JsonToken> tokens) {
        var stack = new ArrayDeque<JsonType>();
        for (var token : tokens) {
            switch (token.type()) {
                case LEFT_BRACE, LEFT_BRACKET -> stack.push(token.type());
                case RIGHT_BRACE -> {
                    ensureOpen(stack);
                    ensureClose(stack, JsonType.LEFT_BRACE);
                }
                case RIGHT_BRACKET -> {
                    ensureOpen(stack);
                    ensureClose(stack, JsonType.LEFT_BRACKET);
                }
            }
        }
        if (!stack.isEmpty()) {
            throw new InvalidJsonException(UNBALANCED_PARENTHESES_ERROR_MESSAGE);
        }
    }

    private static void ensureClose(ArrayDeque<JsonType> stack, JsonType jsonType) {
        var top = stack.pop();
        if (top != jsonType) {
            throw new InvalidJsonException(UNBALANCED_PARENTHESES_ERROR_MESSAGE);
        }
    }

    private static void ensureOpen(Queue<?> tokens) {
        if (tokens.isEmpty()) {
            throw new InvalidJsonException(UNBALANCED_PARENTHESES_ERROR_MESSAGE);
        }
    }
}
