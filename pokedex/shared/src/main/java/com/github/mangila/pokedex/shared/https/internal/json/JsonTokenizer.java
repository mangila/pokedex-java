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

    public List<JsonToken> tokenize(byte[] body) {
        var tokens = new ArrayList<JsonToken>();
        for (int i = 0; i < body.length; i++) {
            var charByte = (char) body[i];
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
                        line.append(charByte);
                        while (true) {
                            charByte = (char) body[++i];
                            if (!Character.isDigit(charByte)) {
                                break;
                            }
                            line.append(charByte);
                        }
                        yield new JsonToken(NUMBER, line.toString());
                    } else if (charByte == '"') {
                        // TODO check for escape sequences
                        while ((charByte = (char) body[++i]) != '"') {
                            line.append(charByte);
                        }
                        yield new JsonToken(STRING, line.toString());
                    } else if (charByte == 't') {
                        i += 3;
                        yield TOKEN_MAP.get(TRUE);
                    } else if (charByte == 'f') {
                        i += 4;
                        yield TOKEN_MAP.get(FALSE);
                    } else if (charByte == 'n') {
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
}
