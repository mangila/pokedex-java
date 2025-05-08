package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.config.JsonParserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.github.mangila.pokedex.shared.json.InvalidJsonException.EMPTY_DATA_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.json.InvalidJsonException.PARSE_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.json.JsonType.RIGHT_BRACE;
import static com.github.mangila.pokedex.shared.json.JsonType.RIGHT_BRACKET;

/**
 * BFS (Breadth-First) traversal of the JSON tree
 */
public class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    private final int maxDepth;
    private JsonTokenQueue queue;
    private int depth;

    public JsonParser(JsonParserConfig config) {
        this.depth = 0;
        this.maxDepth = config.maxDepth();
    }

    public JsonParser() {
        this(new JsonParserConfig(64));
    }

    public Map<String, Object> parseTree(byte[] data) {
        this.queue = JsonTokenizer.tokenizeFrom(data);
        return parseTree();
    }

    public Map<String, Object> parseTree(String data) {
        this.queue = JsonTokenizer.tokenizeFrom(data);
        return parseTree();
    }

    private Map<String, Object> parseTree() {
        if (queue.isEmpty()) {
            throw new InvalidJsonException(EMPTY_DATA_ERROR_MESSAGE);
        }
        var map = new HashMap<String, Object>();
        queue.expect(JsonType.LEFT_BRACE);
        if (queue.peek().type() == RIGHT_BRACE) {
            queue.poll();
            return map;
        }
        while (!queue.isEmpty()) {
            var token = queue.expect(JsonType.STRING);
            queue.expect(JsonType.COLON);
            var value = parseValue();
            map.put((String) token.value(), value);
            this.depth = 0;
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return map;
    }

    private Object parseValue() {
        ensureNotDepthMax();
        var token = queue.peek();
        return switch (token.type()) {
            case STRING, FALSE, TRUE, NULL -> queue.poll().value();
            case NUMBER -> parseNumber();
            case LEFT_BRACE -> {
                this.depth = depth + 1;
                yield parseObject();
            }
            case LEFT_BRACKET -> {
                this.depth = depth + 1;
                yield parseArray();
            }
            default -> throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        };
    }

    private void ensureNotDepthMax() {
        if (depth == maxDepth) {
            log.error("ERR - JSON depth is equal max depth - {}", maxDepth);
            throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        }
    }

    private List<Object> parseArray() {
        queue.expect(JsonType.LEFT_BRACKET);
        var list = new ArrayList<>();
        if (queue.peek().type() == RIGHT_BRACKET) {
            queue.poll();
            return list;
        }

        while (true) {
            var value = parseValue();
            list.add(value);
            if (queue.peek().type() == RIGHT_BRACKET) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }

        return list;
    }

    private Object parseObject() {
        queue.expect(JsonType.LEFT_BRACE);
        Map<String, Object> map = new LinkedHashMap<>();
        if (queue.peek().type() == RIGHT_BRACE) {
            queue.poll();
            return map;
        }
        while (true) {
            var key = queue.expect(JsonType.STRING).value();
            queue.expect(JsonType.COLON);
            var value = parseValue();
            map.put((String) key, value);
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return map;
    }

    // EAFP — Easier to Ask Forgiveness than Permission
    private Object parseNumber() {
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
