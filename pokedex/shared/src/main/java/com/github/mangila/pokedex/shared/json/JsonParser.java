package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.json.model.JsonArray;
import com.github.mangila.pokedex.shared.json.model.JsonObject;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.github.mangila.pokedex.shared.json.InvalidJsonException.EMPTY_DATA_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.json.InvalidJsonException.PARSE_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.json.JsonType.RIGHT_BRACE;
import static com.github.mangila.pokedex.shared.json.JsonType.RIGHT_BRACKET;

public class JsonParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParser.class);
    public static final JsonParser DEFAULT = new JsonParser(JsonParserConfig.DEFAULT);
    private final int maxDepth;

    public JsonParser(JsonParserConfig config) {
        this.maxDepth = config.maxDepth();
    }

    public JsonRoot parseTree(byte[] data) {
        var tokens = JsonTokenizer.tokenizeFrom(data);
        return parseTree(tokens);
    }

    public JsonRoot parseTree(String data) {
        var tokens = JsonTokenizer.tokenizeFrom(data);
        return parseTree(tokens);
    }

    private JsonRoot parseTree(JsonTokenQueue queue) {
        if (queue.isEmpty()) {
            throw new InvalidJsonException(EMPTY_DATA_ERROR_MESSAGE);
        }
        JsonRoot tree = new JsonRoot();
        queue.expect(JsonType.LEFT_BRACE);
        if (queue.peek().type() == RIGHT_BRACE) {
            queue.poll();
            return tree;
        }
        while (!queue.isEmpty()) {
            JsonToken key = queue.expect(JsonType.STRING);
            queue.expect(JsonType.COLON);
            JsonValue value = parseValue(queue, 0);
            tree.add((String) key.value(), value);
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return tree;
    }

    private JsonValue parseValue(JsonTokenQueue queue, int depth) {
        Ensure.notEquals(depth, maxDepth, () -> new InvalidJsonException("JSON depth is equal to max depth: %d".formatted(maxDepth)));
        JsonToken token = queue.peek();
        return switch (token.type()) {
            case STRING, FALSE, TRUE, NULL -> new JsonValue(queue.poll().value());
            case NUMBER -> new JsonValue(parseNumber(queue));
            case LEFT_BRACE -> new JsonValue(parseObject(queue, depth + 1));
            case LEFT_BRACKET -> new JsonValue(parseArray(queue, depth + 1));
            default -> throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        };
    }

    private JsonArray parseArray(JsonTokenQueue queue, int depth) {
        queue.expect(JsonType.LEFT_BRACKET);
        JsonArray jsonArray = new JsonArray();
        if (queue.peek().type() == RIGHT_BRACKET) {
            queue.poll();
            return jsonArray;
        }
        while (true) {
            JsonValue value = parseValue(queue, depth);
            jsonArray.add(value);
            if (queue.peek().type() == RIGHT_BRACKET) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return jsonArray;
    }

    private JsonObject parseObject(JsonTokenQueue queue, int depth) {
        queue.expect(JsonType.LEFT_BRACE);
        if (queue.peek().type() == RIGHT_BRACE) {
            queue.poll();
            return JsonObject.EMPTY;
        }
        JsonObject jsonObject = new JsonObject();
        while (true) {
            JsonToken key = queue.expect(JsonType.STRING);
            queue.expect(JsonType.COLON);
            JsonValue value = parseValue(queue, depth);
            jsonObject.add((String) key.value(), value);
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return jsonObject;
    }

    private Number parseNumber(JsonTokenQueue queue) {
        StringBuilder sb = new StringBuilder();
        while (queue.peek().type() == JsonType.NUMBER) {
            JsonToken token = queue.poll();
            sb.append(token.value());
        }
        String number = sb.toString();
        try {
            // TODO: check -Infinity and NaN
            if (number.contains(".") || number.contains("e") || number.contains("E")) {
                return new BigDecimal(number);
            }
            return new BigInteger(number);
        } catch (NumberFormatException e) {
            throw new InvalidJsonException(
                    "Number format exception - %s".formatted(number)
            );
        }
    }
}
