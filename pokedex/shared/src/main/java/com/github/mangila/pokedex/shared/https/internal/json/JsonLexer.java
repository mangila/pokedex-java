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

    private final byte[] data;
    private int cursor;

    public JsonLexer(byte[] data) {
        this.cursor = 0;
        this.data = data;
    }

    public char read() {
        return (char) data[cursor];
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
            default -> {
                var numberToken = lexNumber(current);
                if (numberToken != null) {
                    yield numberToken;
                }
                throw new InvalidJsonException(String.format("%s - %s - %s", TOKENIZE_ERROR_MESSAGE, current, new String(data)));
            }
        };
    }

    private JsonToken lexString() {
        StringBuilder line = new StringBuilder();
        next(); // skip the first double quote
        while (true) {
            char current = readAndNext();
            if (current == '\\') {
                current = readAndNext();
                if (current == '"') {
                    line.append(current);
                    continue;
                }
            }
            if (current == '"') {
                break;
            }
            line.append(current);
        }
        return new JsonToken(STRING, line.toString());
    }

    private JsonToken lexNumber(char current) {
        if (current == '-' || Character.isDigit(current)) {
            StringBuilder line = new StringBuilder();
            // TODO check for exponential notation
            // TODO ensure if floating point
            line.append(current);
            next();
            while (true) {
                current = readAndNext();
                if (current == '.') {
                    line.append(current);
                    continue;
                }
                if (!Character.isDigit(current)) {
                    break;
                }
                line.append(current);
            }
            return new JsonToken(NUMBER, line.toString());
        }
        return null;
    }

    private JsonToken lexTrue() {
        if (isTrue()) {
            skip(3);
            return TOKEN_MAP.get(TRUE);
        }
        throw new InvalidJsonException(String.format("%s - %s", TOKENIZE_ERROR_MESSAGE, new String(data)));
    }

    private boolean isTrue() {
        return cursor + 3 < data.length && new String(data, cursor, 4).equals("true");
    }

    private JsonToken lexFalse() {
        if (isFalse()) {
            skip(4);
            return TOKEN_MAP.get(FALSE);
        }
        throw new InvalidJsonException(String.format("%s - %s", TOKENIZE_ERROR_MESSAGE, new String(data)));
    }

    private boolean isFalse() {
        return cursor + 4 < data.length && new String(data, cursor, 5).equals("false");
    }

    private JsonToken lexNull() {
        if (isNull()) {
            skip(3);
            return TOKEN_MAP.get(NULL);
        }
        throw new InvalidJsonException(String.format("%s - %s", TOKENIZE_ERROR_MESSAGE, new String(data)));
    }

    private boolean isNull() {
        return cursor + 3 < data.length && new String(data, cursor, 4).equals("null");
    }
}
