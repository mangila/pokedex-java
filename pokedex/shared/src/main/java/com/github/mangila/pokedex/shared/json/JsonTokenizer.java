package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.util.Ensure;

import java.nio.charset.Charset;
import java.util.ArrayDeque;

public class JsonTokenizer {
    public static JsonTokenQueue tokenizeFrom(byte[] data) {
        return tokenize(data);
    }

    public static JsonTokenQueue tokenizeFrom(String data) {
        return tokenize(data.getBytes(Charset.defaultCharset()));
    }

    /**
     * Tokenizes a JSON string into a queue of tokens.
     * Create Queue with 1024 as the start capacity, for some extra performance.
     */
    private static JsonTokenQueue tokenize(byte[] data) {
        Ensure.notNull(data, "json data must not be null");
        if (data.length == 0) {
            return JsonTokenQueue.EMPTY;
        }
        JsonLexer lexer = new JsonLexer(data);
        JsonTokenQueue queue = new JsonTokenQueue(new ArrayDeque<>(1024));
        try (JsonStreamReader reader = lexer.getReader()) {
            int current;
            while ((current = reader.read()) != -1) {
                if (Character.isWhitespace(current)) {
                    continue;
                }
                var token = lexer.lexChar(current);
                queue.add(token);
            }
        } catch (Exception e) {
            throw new InvalidJsonException(e.getMessage(), e);
        }

        return queue;
    }
}
