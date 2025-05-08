package com.github.mangila.pokedex.shared.https.internal.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.github.mangila.pokedex.shared.https.internal.json.InvalidJsonException.EMPTY_DATA_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.https.internal.json.InvalidJsonException.PARSE_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.https.internal.json.JsonType.RIGHT_BRACE;
import static com.github.mangila.pokedex.shared.https.internal.json.JsonType.RIGHT_BRACKET;

public class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    public static Map<String, Object> parseTree(JsonTokenQueue queue) {
        if (queue.isEmpty()) {
            throw new InvalidJsonException(EMPTY_DATA_ERROR_MESSAGE);
        }
        var map = new HashMap<String, Object>();
        queue.expect(JsonType.LEFT_BRACE);
        while (!queue.isEmpty()) {
            var token = queue.expect(JsonType.STRING);
            queue.expect(JsonType.COLON);
            var value = parseValue(queue);
            map.put((String) token.value(), value);
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return map;
    }

    private static Object parseValue(JsonTokenQueue queue) {
        var token = queue.peek();
        return switch (token.type()) {
            case STRING, FALSE, TRUE, NULL -> queue.poll().value();
            case NUMBER -> parseNumber(queue);
            case LEFT_BRACE -> parseObject(queue);
            case LEFT_BRACKET -> parseArray(queue);
            default -> throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        };
    }

    private static List<Object> parseArray(JsonTokenQueue queue) {
        queue.expect(JsonType.LEFT_BRACKET);
        var list = new ArrayList<>();
        if (queue.peek().type() == RIGHT_BRACKET) {
            queue.poll();
            return list;
        }

        while (true) {
            var value = parseValue(queue);
            list.add(value);
            if (queue.peek().type() == RIGHT_BRACKET) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }

        return list;
    }

    private static Object parseObject(JsonTokenQueue queue) {
        queue.expect(JsonType.LEFT_BRACE);
        Map<String, Object> map = new LinkedHashMap<>();
        if (queue.peek().type() == RIGHT_BRACE) {
            queue.poll();
            return map;
        }

        while (true) {
            var key = queue.expect(JsonType.STRING).value();
            queue.expect(JsonType.COLON);
            var value = parseValue(queue);
            map.put((String) key, value);
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }

        return map;
    }

    private static Object parseNumber(JsonTokenQueue queue) {
        var sb = new StringBuilder();
        while (queue.peek().type() == JsonType.NUMBER) {
            var token = queue.poll();
            sb.append(token.value());
        }
        var number = sb.toString();
        try {
            if (number.contains(".") || number.contains("e") || number.contains("E")) {
                return new BigDecimal(number);
            }
            return new BigInteger(number);
        } catch (NumberFormatException e) {
            log.error("ERR - number format exception - {}", number, e);
            throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        }
    }
}
