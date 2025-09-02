package com.github.mangila.pokedex.shared.https.http;

import com.github.mangila.pokedex.shared.https.HttpsUtils;
import com.github.mangila.pokedex.shared.https.model.Status;
import com.github.mangila.pokedex.shared.https.tls.TlsConnectionHandle;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpStatusReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpStatusReader.class);

    public static Status read(TlsConnectionHandle tlsConnectionHandle) throws IOException {
        InputStream inputStream = tlsConnectionHandle.inputStream();
        ByteArrayOutputStream lineBuffer = BufferUtils.newByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == HttpsUtils.END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            lineBuffer.write(current);
            if (HttpsUtils.isCrLf(previous, current)) {
                break;
            }
            previous = current;
        }
        String statusLine = lineBuffer.toString(StandardCharsets.UTF_8).trim();
        LOGGER.debug("Status line: {}", statusLine);
        return Status.from(statusLine);
    }
}
