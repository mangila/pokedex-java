package com.github.mangila.pokedex.shared.https.client;

import com.github.mangila.pokedex.shared.https.model.Body;
import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.tls.TlsConnectionHandler;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import com.github.mangila.pokedex.shared.util.HttpsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class HttpBodyReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpBodyReader.class);

    public Body read(ResponseHeaders responseHeaders, TlsConnectionHandler tlsConnectionHandler) throws IOException {
        InputStream inputStream = tlsConnectionHandler.getConnection().getInputStream();
        if (responseHeaders.isChunked()) {
            LOGGER.debug("Chunked encoding detected");
            if (responseHeaders.isGzip()) {
                LOGGER.debug("Chunked GZIP encoding detected");
                return GzipBodyReader.readChunked(inputStream);
            }
        }
        if (responseHeaders.isGzip()) {
            LOGGER.debug("GZIP encoding detected");
            return GzipBodyReader.read(inputStream, responseHeaders.getContentLength());
        }
        throw new IllegalStateException("Unsupported HTTP body response");
    }

    private static class GzipBodyReader {
        private static Body read(InputStream inputStream, int contentLength) throws IOException {
            byte[] len = inputStream.readNBytes(contentLength);
            byte[] decompressed = new GZIPInputStream(BufferUtils.newByteArrayInputStream(len))
                    .readAllBytes();
            return Body.from(decompressed);
        }

        private static Body readChunked(InputStream inputStream) throws IOException {
            byte[] allChunks = readAllChunks(inputStream);
            byte[] decompressed = new GZIPInputStream(BufferUtils.newByteArrayInputStream(allChunks))
                    .readAllBytes();
            return Body.from(decompressed);
        }

        private static byte[] readAllChunks(InputStream inputStream) throws IOException {
            ByteArrayOutputStream chunkLineBuffer = BufferUtils.newByteArrayOutputStream();
            ByteArrayOutputStream chunkBuffer = BufferUtils.newByteArrayOutputStream(1024);
            int previous = -1;
            while (true) {
                int current = inputStream.read();
                if (current == HttpsUtils.END_OF_STREAM) {
                    break;
                }
                chunkLineBuffer.write(current);
                if (HttpsUtils.isCrLf(previous, current)) {
                    var chunkLine = chunkLineBuffer.toString(StandardCharsets.US_ASCII).trim();
                    if (chunkLine.equals("0")) {
                        inputStream.skipNBytes(inputStream.available());
                        break;
                    } else if (HttpsUtils.HEX_DECIMAL.matcher(chunkLine).matches()) {
                        int chunkSize = Integer.parseInt(chunkLine, 16);
                        chunkBuffer.write(inputStream.readNBytes(chunkSize));
                    }
                    chunkLineBuffer.reset();
                }
                previous = current;
            }

            return chunkBuffer.toByteArray();
        }
    }

}
