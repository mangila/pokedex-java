package com.github.mangila.pokedex.shared.json;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.HexFormat;
import java.util.Map;

import static com.github.mangila.pokedex.shared.json.JsonType.*;

public class JsonLexer {
    private static final Map<JsonType, JsonToken> TOKEN_MAP = Map.of(
            OPEN_BRACE, new JsonToken(OPEN_BRACE, '{'),
            CLOSE_BRACE, new JsonToken(CLOSE_BRACE, '}'),
            OPEN_BRACKET, new JsonToken(OPEN_BRACKET, '['),
            CLOSE_BRACKET, new JsonToken(CLOSE_BRACKET, ']'),
            COMMA, new JsonToken(COMMA, ','),
            COLON, new JsonToken(COLON, ':'),
            TRUE, new JsonToken(TRUE, true),
            FALSE, new JsonToken(FALSE, false),
            NULL, new JsonToken(NULL, "null")
    );

    private static final Map<String, JsonToken> TOKEN_MAP_NUMBER = Map.of(
            "zero", new JsonToken(NUMBER, '0'),
            "one", new JsonToken(NUMBER, '1'),
            "two", new JsonToken(NUMBER, '2'),
            "three", new JsonToken(NUMBER, '3'),
            "four", new JsonToken(NUMBER, '4'),
            "five", new JsonToken(NUMBER, '5'),
            "six", new JsonToken(NUMBER, '6'),
            "seven", new JsonToken(NUMBER, '7'),
            "eight", new JsonToken(NUMBER, '8'),
            "nine", new JsonToken(NUMBER, '9')
    );

    private static final Map<String, JsonToken> TOKEN_MAP_NUMBER_SPECIAL = Map.of(
            "decimal-point", new JsonToken(NUMBER, '.'),
            "negative", new JsonToken(NUMBER, '-'),
            "plus", new JsonToken(NUMBER, '+'),
            "programmer-e", new JsonToken(NUMBER, 'E'),
            "math-e", new JsonToken(NUMBER, 'e')
    );

    private final JsonStreamReader reader;

    public JsonLexer(byte[] data) {
        this.reader = new JsonStreamReader(data);
    }

    public JsonStreamReader getReader() {
        return reader;
    }

    public JsonToken lexChar(int current) throws IOException {
        return switch (current) {
            case '{' -> TOKEN_MAP.get(OPEN_BRACE);
            case '}' -> TOKEN_MAP.get(CLOSE_BRACE);
            case '[' -> TOKEN_MAP.get(OPEN_BRACKET);
            case ']' -> TOKEN_MAP.get(CLOSE_BRACKET);
            case ',' -> TOKEN_MAP.get(COMMA);
            case ':' -> TOKEN_MAP.get(COLON);
            case '"' -> lexString();
            case 't', 'n' -> lexTrueOrNull();
            case 'f' -> lexFalse();
            case '.' -> TOKEN_MAP_NUMBER_SPECIAL.get("decimal-point");
            case '-' -> TOKEN_MAP_NUMBER_SPECIAL.get("negative");
            case '+' -> TOKEN_MAP_NUMBER_SPECIAL.get("plus");
            case '0' -> TOKEN_MAP_NUMBER.get("zero");
            case '1' -> TOKEN_MAP_NUMBER.get("one");
            case '2' -> TOKEN_MAP_NUMBER.get("two");
            case '3' -> TOKEN_MAP_NUMBER.get("three");
            case '4' -> TOKEN_MAP_NUMBER.get("four");
            case '5' -> TOKEN_MAP_NUMBER.get("five");
            case '6' -> TOKEN_MAP_NUMBER.get("six");
            case '7' -> TOKEN_MAP_NUMBER.get("seven");
            case '8' -> TOKEN_MAP_NUMBER.get("eight");
            case '9' -> TOKEN_MAP_NUMBER.get("nine");
            case 'E' -> TOKEN_MAP_NUMBER_SPECIAL.get("programmer-e");
            case 'e' -> TOKEN_MAP_NUMBER_SPECIAL.get("math-e");
            default -> throw new NotValidJsonException("Not a valid JSON character: %c".formatted(current));
        };
    }

    /**
     * String lexing - iterate until a double quote is found
     * if there is an escaping sequence (e.g. \") the next character is read and added to the string
     */
    private JsonToken lexString() throws IOException {
        StringBuilder line = new StringBuilder();
        while (true) {
            int current = reader.read();
            if (current == -1) {
                throw new NotValidJsonException("Unexpected end of data while lexing string");
            }
            if (current == '"') {
                break;
            }
            if (current == '\\') {
                line.append((char) current);
                char escapeChar = (char) reader.read();
                line.append(escapeChar);
                if (isValidEscape(escapeChar)) {
                    continue;
                } else if (escapeChar == 'u') {
                    CharBuffer hex = reader.read(4);
                    line.append(HexFormat.fromHexDigits(hex));
                    continue;
                } else {
                    throw new NotValidJsonException("Not valid escape sequence: %s".formatted(line.toString()));
                }
            }

            line.append((char) current);
        }
        return new JsonToken(STRING, line.toString());
    }

    private boolean isValidEscape(char escapeChar) {
        return escapeChar == '"' ||
               escapeChar == '\\' ||
               escapeChar == '/' ||
               escapeChar == 'b' ||
               escapeChar == 'f' ||
               escapeChar == 'n' ||
               escapeChar == 'r' ||
               escapeChar == 't';
    }

    /**
     * Lex boolean false with the one-off first char since it already have been read
     * - "false" -> "alse"
     */
    private JsonToken lexFalse() throws IOException {
        var read = reader.read(4).toString();
        if (read.equals("alse")) {
            return TOKEN_MAP.get(FALSE);
        }
        throw new NotValidJsonException("Not valid parsing of boolean value false");
    }

    /**
     * Lex boolean true or null with the one-off first char since it already have been read
     * - "true" -> "rue"
     * - "null" -> "ull"
     */
    private JsonToken lexTrueOrNull() throws IOException {
        var read = reader.read(3).toString();
        if (read.equals("rue")) {
            return TOKEN_MAP.get(TRUE);
        } else if (read.equals("ull")) {
            return TOKEN_MAP.get(NULL);
        }
        throw new NotValidJsonException("Not valid parsing of boolean value true or null");
    }
}
