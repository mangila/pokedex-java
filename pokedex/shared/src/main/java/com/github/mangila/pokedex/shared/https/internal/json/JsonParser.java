package com.github.mangila.pokedex.shared.https.internal.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.github.mangila.pokedex.shared.https.internal.json.InvalidJsonException.PARSE_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.https.internal.json.JsonType.RIGHT_BRACE;
import static com.github.mangila.pokedex.shared.https.internal.json.JsonType.RIGHT_BRACKET;

public class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    public static Map<String, Object> parseTree(Queue<JsonToken> tokens) {
        var map = new HashMap<String, Object>();
        var reader = new JsonTokenReader(tokens);
        reader.expect(JsonType.LEFT_BRACE);
        while (!reader.isEmpty()) {
            var token = reader.expect(JsonType.STRING);
            reader.expect(JsonType.COLON);
            var value = parseValue(reader);
            map.put((String) token.value(), value);
            if (reader.peek().type() == RIGHT_BRACE) {
                reader.next();
                break;
            }
            reader.expect(JsonType.COMMA);
        }
        return map;
    }

    private static Object parseValue(JsonTokenReader reader) {
        var token = reader.peek();
        return switch (token.type()) {
            case STRING, FALSE, TRUE, NULL -> reader.next().value();
            case NUMBER -> parseNumber(reader);
            case LEFT_BRACE -> parseObject(reader);
            case LEFT_BRACKET -> parseArray(reader);
            default -> throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        };
    }

    private static List<Object> parseArray(JsonTokenReader reader) {
        reader.expect(JsonType.LEFT_BRACKET);
        var list = new ArrayList<>();
        if (reader.peek().type() == RIGHT_BRACKET) {
            reader.next();
            return list;
        }

        while (true) {
            var value = parseValue(reader);
            list.add(value);
            if (reader.peek().type() == RIGHT_BRACKET) {
                reader.next();
                break;
            }
            reader.expect(JsonType.COMMA);
        }

        return list;
    }

    private static Object parseObject(JsonTokenReader reader) {
        reader.expect(JsonType.LEFT_BRACE);
        Map<String, Object> map = new LinkedHashMap<>();
        if (reader.peek().type() == RIGHT_BRACE) {
            reader.next();
            return map;
        }

        while (true) {
            var key = reader.expect(JsonType.STRING).value();
            reader.expect(JsonType.COLON);
            var value = parseValue(reader);
            map.put((String) key, value);
            if (reader.peek().type() == RIGHT_BRACE) {
                reader.next();
                break;
            }
            reader.expect(JsonType.COMMA);
        }

        return map;
    }

    private static Object parseNumber(JsonTokenReader reader) {
        var sb = new StringBuilder();
        while (reader.peek().type() == JsonType.NUMBER) {
            var token = reader.next();
            sb.append(token.value());
        }
        return new BigDecimal(sb.toString());
    }
}
