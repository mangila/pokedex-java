package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.Map;

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

    public char readChar() {
        return (char) data[cursor];
    }

    public int nextChar() {
        this.cursor = cursor + 1;
        return cursor;
    }

    public void nextChar(int count) {
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
            default -> {
                var numberToken = lexNumber(current);
                if (numberToken != null) {
                    yield numberToken;
                }
                var stringToken = lexString(current);
                if (stringToken != null) {
                    yield stringToken;
                }
                var trueToken = lexTrue(current);
                if (trueToken != null) {
                    yield trueToken;
                }
                var falseToken = lexFalse(current);
                if (falseToken != null) {
                    yield falseToken;
                }
                var nullToken = lexNull(current);
                if (nullToken != null) {
                    yield nullToken;
                }
                throw new InvalidJsonException(String.format("Invalid json data cannot tokenize - invalid character: %s - %s", current, new String(data)));
            }
        };
    }

    private JsonToken lexString(char current) {
        if (current == '"') {
            StringBuilder line = new StringBuilder();
            while (true) {
                current = (char) data[nextChar()];
                if (current == '\\') {
                    current = (char) data[nextChar()];
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
        return null;
    }

    private JsonToken lexNumber(char current) {
        if (current == '-' || Character.isDigit(current)) {
            StringBuilder line = new StringBuilder();
            // TODO check for exponential notation
            line.append(current);
            while (true) {
                current = (char) data[nextChar()];
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

    private JsonToken lexTrue(char current) {
        if (isTrue(current)) {
            nextChar(3);
            return new JsonToken(TRUE, true);
        }
        return null;
    }

    private boolean isTrue(char current) {
        return current == 't' && cursor + 3 < data.length && new String(data, cursor, 4).equals("true");
    }

    private JsonToken lexFalse(char current) {
        if (isFalse(current)) {
            nextChar(4);
            return new JsonToken(FALSE, false);
        }
        return null;
    }

    private boolean isFalse(char current) {
        return current == 'f' && cursor + 4 < data.length && new String(data, cursor, 5).equals("false");
    }

    private JsonToken lexNull(char current) {
        if (isNull(current)) {
            nextChar(3);
            return new JsonToken(NULL, null);
        }
        return null;
    }

    private boolean isNull(char current) {
        return current == 'n' && cursor + 3 < data.length && new String(data, cursor, 4).equals("null");
    }
}
