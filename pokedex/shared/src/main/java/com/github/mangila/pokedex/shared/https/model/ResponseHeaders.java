package com.github.mangila.pokedex.shared.https.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResponseHeaders extends AbstractHeaders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHeaders.class);

    public boolean isGzip() {
        return headerExists("Content-Encoding") && getRaw("Content-Encoding").contains("gzip");
    }

    public boolean isJson() {
        return headerExists("Content-Type") && getRaw("Content-Type").contains("application/json");
    }

    public boolean isChunked() {
        return headerExists("Transfer-Encoding") && getRaw("Transfer-Encoding").contains("chunked");
    }

    public int getContentLength() {
        return getHeaderAsInt("Content-Length", 0);
    }

    private int getHeaderAsInt(String headerName, int defaultValue) {
        if (!headerExists(headerName)) {
            return defaultValue;
        }
        String raw = getRaw(headerName);
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            LOGGER.error("{} - Not valid header value: {} - Fallback to default value: {}", headerName, raw, defaultValue);
            return defaultValue;
        }
    }

}
