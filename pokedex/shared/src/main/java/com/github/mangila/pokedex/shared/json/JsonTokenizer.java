package com.github.mangila.pokedex.shared.json;

import java.util.ArrayDeque;

public class JsonTokenizer {
    public static JsonTokenQueue tokenizeFrom(byte[] data) {
        if (data == null || data.length == 0) {
            return JsonTokenQueue.EMPTY;
        }
        return tokenize(data);
    }

    private static JsonTokenQueue tokenize(byte[] data) {
        JsonLexer lexer = new JsonLexer(data);
        JsonTokenQueue queue = new JsonTokenQueue(new ArrayDeque<>(estimateCapacity(data.length)));
        try (JsonStreamReader reader = lexer.getReader()) {
            int current;
            while ((current = reader.read()) != -1) {
                if (Character.isWhitespace(current)) {
                    continue;
                }
                JsonToken token = lexer.lexChar(current);
                queue.add(token);
            }
        } catch (NotValidJsonException e) {
            throw e;
        } catch (Exception e) {
            throw new NotValidJsonException(e.getMessage(), e);
        }
        return queue;
    }


    /**
     * Estimates the initial capacity for a data structure based on the size of the provided byte array.
     *
     * @param dataLength the byte array whose length is used to estimate the capacity
     * @return the estimated capacity, which is generally one-fourth of the byte array size,
     * or a minimum value of 16 if the calculated capacity does not exceed 32
     */
    private static int estimateCapacity(int dataLength) {
        int capacity = dataLength / 4;
        if (capacity > 32) {
            return capacity;
        }
        return 16;
    }
}
