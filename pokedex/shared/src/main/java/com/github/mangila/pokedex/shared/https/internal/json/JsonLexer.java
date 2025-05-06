package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.Map;

import static com.github.mangila.pokedex.shared.https.internal.json.InvalidJsonException.TOKENIZE_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.https.internal.json.JsonType.*;

public class JsonLexer {

    private static final Map<JsonType, JsonToken> TOKEN_MAP = Map.of(
            LEFT_BRACE, new JsonToken(LEFT_BRACE, '{'),
            RIGHT_BRACE, new JsonToken(RIGHT_BRACE, '}'),
            LEFT_BRACKET, new JsonToken(LEFT_BRACKET, '['),
            RIGHT_BRACKET, new JsonToken(RIGHT_BRACKET, ']'),
            COMMA, new JsonToken(COMMA, ','),
            COLON, new JsonToken(COLON, ':'),
            TRUE, new JsonToken(TRUE, true),
            FALSE, new JsonToken(FALSE, false),
            NULL, new JsonToken(NULL, null)
    );

    private static final Map<String, JsonToken> TOKEN_MAP_NUMBER = Map.of(
            "zero", new JsonToken(NUMBER, '0'),
            "one", new JsonToken(NUMBER, '1'),
            "two", new JsonToken(NUMBER, '2'),
            "three", new JsonToken(NUMBER, '3'),
            "four", new JsonToken(NUMBER, '4'),
            "five", new JsonToken(NUMBER, '5'),
            "six", new JsonToken(NUMBER, '6'),
            "seven", new JsonToken(NUMBER, '7'),
            "eight", new JsonToken(NUMBER, '8'),
            "nine", new JsonToken(NUMBER, '9')
    );

    private static final Map<String, JsonToken> TOKEN_MAP_NUMBER_SPECIAL = Map.of(
            "negative", new JsonToken(NUMBER, '-'),
            "plus", new JsonToken(NUMBER, '+'),
            "programmer-e", new JsonToken(NUMBER, 'E'),
            "math-e", new JsonToken(NUMBER, 'e')
    );

    private final byte[] data;
    private int cursor;

    public JsonLexer(byte[] data) {
        this.cursor = 0;
        this.data = data;
    }

    // 	EAFP - Easier to Ask Forgiveness than Permission
    public char read() {
        try {
            return (char) data[cursor];
        } catch (Exception e) {
            throw new InvalidJsonException(TOKENIZE_ERROR_MESSAGE);
        }
    }

    public void next() {
        this.cursor = cursor + 1;
    }

    public char readAndNext() {
        char current = read();
        next();
        return current;
    }

    public void skip(int count) {
        this.cursor = cursor + count;
    }

    public boolean finishedReadingData() {
        return cursor >= data.length;
    }

    public JsonToken lexChar(char current) {
        return switch (current) {
            case '{' -> TOKEN_MAP.get(LEFT_BRACE);
            case '}' -> TOKEN_MAP.get(RIGHT_BRACE);
            case '[' -> TOKEN_MAP.get(LEFT_BRACKET);
            case ']' -> TOKEN_MAP.get(RIGHT_BRACKET);
            case ',' -> TOKEN_MAP.get(COMMA);
            case ':' -> TOKEN_MAP.get(COLON);
            case '"' -> lexString();
            case 't' -> lexTrue();
            case 'f' -> lexFalse();
            case 'n' -> lexNull();
            case '-' -> TOKEN_MAP_NUMBER_SPECIAL.get("negative");
            case '+' -> TOKEN_MAP_NUMBER_SPECIAL.get("plus");
            case '0' -> TOKEN_MAP_NUMBER.get("zero");
            case '1' -> TOKEN_MAP_NUMBER.get("one");
            case '2' -> TOKEN_MAP_NUMBER.get("two");
            case '3' -> TOKEN_MAP_NUMBER.get("three");
            case '4' -> TOKEN_MAP_NUMBER.get("four");
            case '5' -> TOKEN_MAP_NUMBER.get("five");
            case '6' -> TOKEN_MAP_NUMBER.get("six");
            case '7' -> TOKEN_MAP_NUMBER.get("seven");
            case '8' -> TOKEN_MAP_NUMBER.get("eight");
            case '9' -> TOKEN_MAP_NUMBER.get("nine");
            case 'E' -> TOKEN_MAP_NUMBER_SPECIAL.get("programmer-e");
            case 'e' -> TOKEN_MAP_NUMBER_SPECIAL.get("math-e");
            default -> throw new InvalidJsonException(TOKENIZE_ERROR_MESSAGE);
        };
    }

    private JsonToken lexString() {
        StringBuilder line = new StringBuilder();
        next(); // skip the first double quote
        while (read() != '"') {
            char current = readAndNext();
            if (current == '\\') {
                line.append(current);
                current = readAndNext();
                line.append(current);
                continue;
            }
            line.append(current);
        }
        return new JsonToken(STRING, line.toString());
    }

    // LBYL - Look Before You Leap
    private JsonToken lexTrue() {
        if (isTrue()) {
            skip(3);
            return TOKEN_MAP.get(TRUE);
        }
        throw new InvalidJsonException(TOKENIZE_ERROR_MESSAGE);
    }

    private boolean isTrue() {
        return cursor + 3 < data.length && new String(data, cursor, 4).equals("true");
    }

    private JsonToken lexFalse() {
        if (isFalse()) {
            skip(4);
            return TOKEN_MAP.get(FALSE);
        }
        throw new InvalidJsonException(TOKENIZE_ERROR_MESSAGE);
    }

    private boolean isFalse() {
        return cursor + 4 < data.length && new String(data, cursor, 5).equals("false");
    }

    private JsonToken lexNull() {
        if (isNull()) {
            skip(3);
            return TOKEN_MAP.get(NULL);
        }
        throw new InvalidJsonException(TOKENIZE_ERROR_MESSAGE);
    }

    private boolean isNull() {
        return cursor + 3 < data.length && new String(data, cursor, 4).equals("null");
    }
}
