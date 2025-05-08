package com.github.mangila.pokedex.shared.https.internal.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Objects;

public class JsonTokenizer {

    private static final Logger log = LoggerFactory.getLogger(JsonTokenizer.class);

    public static JsonTokenQueue tokenizeFrom(byte[] data) {
        return tokenize(data);
    }

    public static JsonTokenQueue tokenizeFrom(String data) {
        return tokenize(data.getBytes(Charset.defaultCharset()));
    }

    /**
     * Tokenizes a JSON string into a queue of tokens.
     * Try-with-resources is used to close the reader automatically
     * might be completely unnecessary, since we read an in-memory byte array
     * a good practice anyway.
     * Create Queue with 1024 as the start size - for some extra performance.
     */
    private static JsonTokenQueue tokenize(byte[] data) {
        Objects.requireNonNull(data, "json data must not be null");
        var lexer = new JsonLexer(data);
        var queue = new JsonTokenQueue(new ArrayDeque<>(1024));
        try (var reader = lexer.getReader()) {
            int current;
            while ((current = reader.read()) != -1) {
                if (Character.isWhitespace(current)) {
                    continue;
                }
                var token = lexer.lexChar(current);
                queue.add(token);
            }
        } catch (Exception e) {
            log.error("ERR", e);
            throw new InvalidJsonException(InvalidJsonException.TOKENIZE_ERROR_MESSAGE);
        }

        return queue;
    }
}
