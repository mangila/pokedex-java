package com.github.mangila.pokedex.shared.https.http;

import com.github.mangila.pokedex.shared.https.HttpsUtils;
import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.https.tls.TlsConnectionHandle;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpHeaderReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHeaderReader.class);

    private static final int MAX_HEADERS_SIZE = 1024 * 8;

    public static ResponseHeaders read(TlsConnectionHandle tlsConnectionHandle) throws IOException {
        InputStream inputStream = tlsConnectionHandle.inputStream();
        ByteArrayOutputStream lineBuffer = BufferUtils.newByteArrayOutputStream();
        ResponseHeaders responseHeaders = new ResponseHeaders();
        int current;
        int previous = -1;
        while (true) {
            current = inputStream.read();
            if (current == HttpsUtils.END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            lineBuffer.write(current);
            if (lineBuffer.size() >= MAX_HEADERS_SIZE) {
                throw new IOException("Headers exceed maximum size of " + MAX_HEADERS_SIZE + " bytes");
            }
            if (HttpsUtils.isCrLf(previous, current)) {
                String rawHeader = lineBuffer.toString(StandardCharsets.UTF_8).trim();
                if (rawHeader.isBlank()) {
                    LOGGER.debug("End of Headers");
                    break;
                }
                String[] parts = rawHeader.split(": ", 2);
                if (parts.length != 2) {
                    LOGGER.warn("Malformed header line: {}", rawHeader);
                    continue;
                }
                responseHeaders.putRaw(parts[0], parts[1]);
                lineBuffer.reset();
            }
            previous = current;
        }
        return responseHeaders;
    }
}
