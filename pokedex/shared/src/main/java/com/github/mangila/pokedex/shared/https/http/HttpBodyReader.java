package com.github.mangila.pokedex.shared.https.http;

import com.github.mangila.pokedex.shared.https.HttpsUtils;
import com.github.mangila.pokedex.shared.https.model.Body;
import com.github.mangila.pokedex.shared.https.model.ResponseHeaders;
import com.github.mangila.pokedex.shared.https.tls.TlsConnectionHandle;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public final class HttpBodyReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpBodyReader.class);

    public static Body read(ResponseHeaders responseHeaders, TlsConnectionHandle tlsConnectionHandle) throws IOException {
        InputStream inputStream = tlsConnectionHandle.inputStream();
        if (responseHeaders.isChunked()) {
            LOGGER.debug("Chunked Transfer Encoding detected");
            if (responseHeaders.isGzip()) {
                LOGGER.debug("Chunked GZIP encoding detected");
                return GzipBodyReader.readChunked(inputStream);
            }
            return BodyReader.readChunked(inputStream);
        }
        int contentLength = responseHeaders.getContentLength();
        if (responseHeaders.isGzip()) {
            LOGGER.debug("GZIP encoding detected");
            return GzipBodyReader.read(inputStream, contentLength);
        }
        return BodyReader.read(inputStream, contentLength);
    }

    private static class BodyReader {
        private static Body read(InputStream inputStream, int contentLength) throws IOException {
            Content content = readContentLength(inputStream, contentLength);
            return Body.from(content.value);
        }

        public static Body readChunked(InputStream inputStream) throws IOException {
            Content content = readAllChunks(inputStream);
            return Body.from(content.value);
        }
    }

    private static class GzipBodyReader {
        private static Body read(InputStream inputStream, int contentLength) throws IOException {
            Content gzipBody = readContentLength(inputStream, contentLength);
            Content decompressed = decompress(gzipBody);
            return Body.from(decompressed.value);
        }

        private static Body readChunked(InputStream inputStream) throws IOException {
            Content gzipBody = readAllChunks(inputStream);
            Content decompressed = decompress(gzipBody);
            return Body.from(decompressed.value);
        }

        private static Content decompress(Content compressedContent) throws IOException {
            byte[] compressedBytes = compressedContent.value;
            LOGGER.debug("Compressed {} bytes", compressedBytes.length);
            ByteArrayInputStream compressedStream = BufferUtils.newByteArrayInputStream(compressedBytes);
            byte[] decompressedBytes = new GZIPInputStream(compressedStream).readAllBytes();
            LOGGER.debug("Decompressed {} bytes", decompressedBytes.length);
            return new Content(decompressedBytes);
        }
    }

    private record Content(byte[] value) {
    }

    private static Content readContentLength(InputStream inputStream, int contentLength) throws IOException {
        LOGGER.debug("Content-Length: {}", contentLength);
        byte[] readNBytes = inputStream.readNBytes(contentLength);
        return new Content(readNBytes);
    }

    private static Content readAllChunks(InputStream inputStream) throws IOException {
        ByteArrayOutputStream chunkBuffer = BufferUtils.newByteArrayOutputStream(1024);
        ByteArrayOutputStream lineBuffer = BufferUtils.newByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == HttpsUtils.END_OF_STREAM) {
                break;
            }
            lineBuffer.write(current);
            if (HttpsUtils.isCrLf(previous, current)) {
                String chunkLine = lineBuffer.toString().trim();
                if (chunkLine.equals("0")) {
                    LOGGER.debug("End of Chunks");
                    inputStream.skipNBytes(inputStream.available());
                    break;
                } else if (HttpsUtils.HEX_DECIMAL.matcher(chunkLine).matches()) {
                    int chunkSize = Integer.parseInt(chunkLine, 16);
                    LOGGER.debug("Chunk size: {} - Hex: {}", chunkSize, chunkLine);
                    chunkBuffer.write(inputStream.readNBytes(chunkSize));
                }
                lineBuffer.reset();
            }
            previous = current;
        }

        return new Content(chunkBuffer.toByteArray());
    }
}
