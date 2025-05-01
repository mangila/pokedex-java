package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.mangila.pokedex.shared.https.internal.json.JsonType.*;

public class JsonTokenizer {

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

    public List<JsonToken> tokenizeFrom(byte[] data) {
        return tokenize(data);
    }

    public List<JsonToken> tokenizeFrom(String data) {
        return tokenize(data.getBytes());
    }

    private List<JsonToken> tokenize(byte[] data) {
        var tokens = new ArrayList<JsonToken>();
        for (int i = 0; i < data.length; i++) {
            var charByte = (char) data[i];
            JsonToken token = switch (charByte) {
                case '{' -> TOKEN_MAP.get(LEFT_BRACE);
                case '}' -> TOKEN_MAP.get(RIGHT_BRACE);
                case '[' -> TOKEN_MAP.get(LEFT_BRACKET);
                case ']' -> TOKEN_MAP.get(RIGHT_BRACKET);
                case ',' -> TOKEN_MAP.get(COMMA);
                case ':' -> TOKEN_MAP.get(COLON);
                default -> {
                    StringBuilder line = new StringBuilder();
                    if (Character.isWhitespace(charByte)) {
                        yield null;
                    } else if (Character.isDigit(charByte) || charByte == '-') {
                        // TODO check for exponential notation
                        line.append(charByte);
                        while (true) {
                            charByte = (char) data[++i];
                            if (!Character.isDigit(charByte)) {
                                break;
                            }
                            line.append(charByte);
                        }
                        yield new JsonToken(NUMBER, line.toString());
                    } else if (charByte == '"') {
                        // TODO check for escape sequences
                        while ((charByte = (char) data[++i]) != '"') {
                            line.append(charByte);
                        }
                        yield new JsonToken(STRING, line.toString());
                    } else if (isTrue(charByte, i, data)) {
                        i += 3;
                        yield TOKEN_MAP.get(TRUE);
                    } else if (isFalse(charByte, i, data)) {
                        i += 4;
                        yield TOKEN_MAP.get(FALSE);
                    } else if (isNull(charByte, i, data)) {
                        i += 3;
                        yield TOKEN_MAP.get(NULL);
                    } else {
                        throw new InvalidJsonException("Unexpected character: " + charByte);
                    }
                }
            };
            tokens.add(token);
        }

        return tokens;
    }

    private static boolean isTrue(char charByte, int index, byte[] body) {
        return charByte == 't' && index + 3 < body.length && new String(body, index, 4).equals("true");
    }

    private static boolean isFalse(char charByte, int index, byte[] body) {
        return charByte == 'f' && index + 4 < body.length && new String(body, index, 5).equals("false");
    }

    private static boolean isNull(char charByte, int index, byte[] body) {
        return charByte == 'n' && index + 3 < body.length && new String(body, index, 4).equals("null");
    }
}
