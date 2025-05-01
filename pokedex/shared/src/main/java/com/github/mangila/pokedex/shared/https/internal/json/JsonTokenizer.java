package com.github.mangila.pokedex.shared.https.internal.json;

import java.util.ArrayList;
import java.util.List;

public class JsonTokenizer {

    public List<JsonToken> tokenize(byte[] body) {
        var l = new ArrayList<JsonToken>();
        for (int i = 0; i < body.length; i++) {
            var c = (char) body[i];
            JsonToken s = switch (c) {
                case '{' -> new JsonToken(JsonType.LEFT_BRACE, c);
                case '}' -> new JsonToken(JsonType.RIGHT_BRACE, c);
                case '[' -> new JsonToken(JsonType.LEFT_BRACKET, c);
                case ']' -> new JsonToken(JsonType.RIGHT_BRACKET, c);
                case ',' -> new JsonToken(JsonType.COMMA, c);
                case ':' -> new JsonToken(JsonType.COLON, c);
                default -> {
                    char j = c;
                    StringBuilder line = new StringBuilder();
                    if (Character.isWhitespace(c)) {
                        yield null;
                    }
                    if (Character.isDigit(c) || c == '-') {
                        line.append(j);
                        while (true) {
                            j = (char) body[++i];
                            if (!Character.isDigit(j)) {
                                break;
                            }
                            line.append(j);
                        }
                        yield new JsonToken(JsonType.NUMBER, line.toString());
                    }
                    if (c == '"') {
                        // TODO check for escape sequences
                        while ((j = (char) body[++i]) != '"') {
                            line.append(j);
                        }
                        yield new JsonToken(JsonType.STRING, line.toString());
                    } else {
                        if (c == 't') {
                            i += 3;
                            yield new JsonToken(JsonType.TRUE, true);
                        } else if (c == 'f') {
                            i += 4;
                            yield new JsonToken(JsonType.FALSE, false);
                        } else {
                            i += 3;
                            yield new JsonToken(JsonType.NULL, null);
                        }
                    }
                }
            };
            l.add(s);
        }

        return l;
    }
}
