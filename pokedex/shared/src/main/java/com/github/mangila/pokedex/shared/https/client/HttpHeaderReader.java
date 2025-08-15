package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.tls.TlsConnectionHandler;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import com.github.mangila.pokedex.shared.util.HttpsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class HttpHeaderReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHeaderReader.class);

    public ResponseHeaders read(TlsConnectionHandler tlsConnectionHandler) throws IOException {
        InputStream inputStream = tlsConnectionHandler.getConnection().getInputStream();
        return readHeaders(inputStream);
    }

    private static ResponseHeaders readHeaders(InputStream inputStream) throws IOException {
        ByteArrayOutputStream lineBuffer = BufferUtils.newByteArrayOutputStream(1024);
        ResponseHeaders responseHeaders = new ResponseHeaders();
        int current;
        int previous = -1;
        while (true) {
            current = inputStream.read();
            if (current == HttpsUtils.END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            lineBuffer.write(current);
            if (HttpsUtils.isCrLf(previous, current)) {
                String rawHeader = lineBuffer.toString(Charset.defaultCharset()).trim();
                LOGGER.debug("Header: {}", rawHeader);
                if (rawHeader.isBlank()) {
                    break;
                }
                String[] parts = rawHeader.split(": ", 2);
                responseHeaders.putRaw(parts[0], parts[1]);
                lineBuffer.reset();
            }
            previous = current;
        }
        return responseHeaders;
    }
}
