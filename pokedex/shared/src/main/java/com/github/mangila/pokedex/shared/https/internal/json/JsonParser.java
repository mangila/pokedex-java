package com.github.mangila.pokedex.shared.https.internal.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    public Object parse(List<JsonToken> tokens) {
        tokens.forEach(jsonToken -> {
            log.debug("{} - {}", jsonToken.type(), jsonToken.value());
        });
        return new Object();
    }
}
