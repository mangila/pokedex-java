package com.github.mangila.pokedex.shared.https.model;

public final class ResponseHeaders extends AbstractHeaders {
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
        return Integer.parseInt(getRaw("Content-Length"));
    }
}
