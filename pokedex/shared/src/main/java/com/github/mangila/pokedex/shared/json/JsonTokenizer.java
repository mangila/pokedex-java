package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.util.ArrayUtils;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayDeque;

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
     * Create Queue with 1024 as the startOffset size - for some extra performance.
     */
    private static JsonTokenQueue tokenize(byte[] data) {
        Ensure.notNull(data, "json data must not be null");
        if (ArrayUtils.isEmpty(data)) {
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
            log.error("ERR", e);
            throw new InvalidJsonException(InvalidJsonException.TOKENIZE_ERROR_MESSAGE);
        }

        return queue;
    }
}
