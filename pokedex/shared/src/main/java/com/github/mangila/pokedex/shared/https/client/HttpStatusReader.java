package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.Status;
import com.github.mangila.pokedex.shared.tls.TlsConnectionHandler;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import com.github.mangila.pokedex.shared.util.HttpsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class HttpStatusReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpStatusReader.class);

    public Status read(TlsConnectionHandler tlsConnectionHandler) throws IOException {
        InputStream inputStream = tlsConnectionHandler.getConnection().getInputStream();
        return readStatusLine(inputStream);
    }

    private static Status readStatusLine(InputStream inputStream) throws IOException {
        ByteArrayOutputStream lineBuffer = BufferUtils.newByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == HttpsUtils.END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            lineBuffer.write(current);
            // Break if the status line is complete via CR+LF
            // also check if the line-buffer has some content
            if (HttpsUtils.isCrLf(previous, current) && lineBuffer.size() > 8) {
                break;
            }
            previous = current;
        }
        String statusLine = lineBuffer.toString(Charset.defaultCharset()).trim();
        LOGGER.debug("Status line: {}", statusLine);
        return Status.from(statusLine);
    }
}
