package com.github.mangila.pokedex.shared.json;

import com.github.mangila.pokedex.shared.json.model.JsonArray;
import com.github.mangila.pokedex.shared.json.model.JsonObject;
import com.github.mangila.pokedex.shared.json.model.JsonTree;
import com.github.mangila.pokedex.shared.json.model.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.github.mangila.pokedex.shared.json.InvalidJsonException.EMPTY_DATA_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.json.InvalidJsonException.PARSE_ERROR_MESSAGE;
import static com.github.mangila.pokedex.shared.json.JsonType.RIGHT_BRACE;
import static com.github.mangila.pokedex.shared.json.JsonType.RIGHT_BRACKET;

/**
 * DFS (Depth-First) traversal of the JSON tree
 * O(2n) = O(n) since it has to tokenize and then traverse the whole tree
 */
public class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);
    private static final JsonParser INSTANCE = new JsonParser(64);

    public static JsonParser getInstance() {
        return INSTANCE;
    }

    private int maxDepth;

    private JsonParser(int maxDepth) {
        log.info("Create new json parser with default max depth {}", maxDepth);
        this.maxDepth = maxDepth;
    }

    public JsonTree parseTree(byte[] data) {
        var tokens = JsonTokenizer.tokenizeFrom(data);
        return parseTree(tokens);
    }

    public JsonTree parseTree(String data) {
        var tokens = JsonTokenizer.tokenizeFrom(data);
        return parseTree(tokens);
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    private JsonTree parseTree(JsonTokenQueue queue) {
        if (queue.isEmpty()) {
            throw new InvalidJsonException(EMPTY_DATA_ERROR_MESSAGE);
        }
        var tree = new JsonTree();
        queue.expect(JsonType.LEFT_BRACE);
        if (queue.peek().type() == RIGHT_BRACE) {
            queue.poll();
            return tree;
        }
        while (!queue.isEmpty()) {
            var key = queue.expect(JsonType.STRING);
            queue.expect(JsonType.COLON);
            var value = parseValue(queue, 0);
            tree.add((String) key.value(), value);
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return tree;
    }

    // LBYL - Look Before You Leap
    private JsonValue parseValue(JsonTokenQueue queue, int depth) {
        ensureNotDepthMax(depth);
        var token = queue.peek();
        return switch (token.type()) {
            case STRING, FALSE, TRUE, NULL -> new JsonValue(queue.poll().value());
            case NUMBER -> new JsonValue(parseNumber(queue));
            case LEFT_BRACE -> new JsonValue(parseObject(queue, depth + 1));
            case LEFT_BRACKET -> new JsonValue(parseArray(queue, depth + 1));
            default -> throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        };
    }

    private void ensureNotDepthMax(int depth) {
        if (depth == maxDepth) {
            log.error("ERR - JSON depth is equal max depth - {}", maxDepth);
            throw new InvalidJsonException(PARSE_ERROR_MESSAGE);
        }
    }

    private JsonArray parseArray(JsonTokenQueue queue, int depth) {
        queue.expect(JsonType.LEFT_BRACKET);
        var jsonArray = new JsonArray();
        if (queue.peek().type() == RIGHT_BRACKET) {
            queue.poll();
            return jsonArray;
        }
        while (true) {
            var value = parseValue(queue, depth);
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
        var jsonObject = new JsonObject();
        while (true) {
            var key = queue.expect(JsonType.STRING).value();
            queue.expect(JsonType.COLON);
            var value = parseValue(queue, depth);
            jsonObject.add((String) key, value);
            if (queue.peek().type() == RIGHT_BRACE) {
                queue.poll();
                break;
            }
            queue.expect(JsonType.COMMA);
        }
        return jsonObject;
    }

    // EAFP — Easier to Ask Forgiveness than Permission
    private Number parseNumber(JsonTokenQueue queue) {
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
