package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.json.model.JsonArray;
import com.github.mangila.pokedex.shared.json.model.JsonObject;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.github.mangila.pokedex.shared.json.JsonType.CLOSE_BRACE;
import static com.github.mangila.pokedex.shared.json.JsonType.CLOSE_BRACKET;

public class JsonParser {
    public static final JsonParser DEFAULT = new JsonParser(JsonParserConfig.DEFAULT);
    private final int maxDepth;

    public JsonParser(JsonParserConfig config) {
        this.maxDepth = config.maxDepth();
    }

    public JsonRoot parseTree(byte[] data) {
        JsonTokenQueue jsonTokenQueue = JsonTokenizer.tokenizeFrom(data);
        return parseTree(jsonTokenQueue);
    }

    public JsonRoot parseTree(String data) {
        JsonTokenQueue jsonTokenQueue = JsonTokenizer.tokenizeFrom(data.getBytes());
        return parseTree(jsonTokenQueue);
    }

    private JsonRoot parseTree(JsonTokenQueue queue) {
        if (queue.isEmpty()) {
            return JsonRoot.EMPTY;
        }
        queue.expect(JsonType.OPEN_BRACE);
        if (isClosing(queue.peek(), CLOSE_BRACE)) {
            queue.poll();
            return JsonRoot.EMPTY;
        }
        JsonRoot jsonRoot = new JsonRoot();
        while (!queue.isEmpty()) {
            JsonToken key = queue.expect(JsonType.STRING);
            queue.expect(JsonType.COLON);
            JsonValue value = parseValue(queue, 0);
            jsonRoot.add((String) key.value(), value);
            if (isClosing(queue.peek(), CLOSE_BRACE)) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return jsonRoot;
    }

    private JsonValue parseValue(JsonTokenQueue queue, int depth) {
        Ensure.notEquals(depth, maxDepth, () -> new NotValidJsonException("JSON depth is equal to max depth: %d".formatted(maxDepth)));
        JsonToken token = queue.peek();
        Ensure.notNull(token, () -> new NotValidJsonException("Unexpected end of JSON"));
        return switch (token.type()) {
            case STRING, FALSE, TRUE, NULL -> {
                JsonToken jsonToken = queue.poll();
                Ensure.notNull(jsonToken, () -> new NotValidJsonException("Unexpected end of JSON"));
                yield new JsonValue(jsonToken.value());
            }
            case NUMBER -> new JsonValue(parseNumber(queue));
            case OPEN_BRACE -> new JsonValue(parseObject(queue, depth + 1));
            case OPEN_BRACKET -> new JsonValue(parseArray(queue, depth + 1));
            default -> throw new NotValidJsonException("Token type not supported: %s".formatted(token.type()));
        };
    }

    private JsonArray parseArray(JsonTokenQueue queue, int depth) {
        queue.expect(JsonType.OPEN_BRACKET);
        if (isClosing(queue.peek(), CLOSE_BRACKET)) {
            queue.poll();
            return JsonArray.EMPTY;
        }
        JsonArray jsonArray = new JsonArray();
        while (true) {
            JsonValue value = parseValue(queue, depth);
            jsonArray.add(value);
            if (isClosing(queue.peek(), CLOSE_BRACKET)) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return jsonArray;
    }

    private JsonObject parseObject(JsonTokenQueue queue, int depth) {
        queue.expect(JsonType.OPEN_BRACE);
        if (isClosing(queue.peek(), CLOSE_BRACE)) {
            queue.poll();
            return JsonObject.EMPTY;
        }
        JsonObject jsonObject = new JsonObject();
        while (true) {
            JsonToken key = queue.expect(JsonType.STRING);
            queue.expect(JsonType.COLON);
            JsonValue value = parseValue(queue, depth);
            jsonObject.add((String) key.value(), value);
            if (isClosing(queue.peek(), CLOSE_BRACE)) {
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
            throw new NotValidJsonException("Number format exception - %s".formatted(number), e);
        }
    }

    private static boolean isClosing(@Nullable JsonToken token, JsonType type) {
        Ensure.notNull(token, () -> new NotValidJsonException("Unexpected end of JSON"));
        return token.type() == type;
    }
}
