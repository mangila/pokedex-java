package com.github.mangila.pokedex.shared.https.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);
    private static final int END_OF_STREAM = -1;

    public static String readStatusLine(InputStream inputStream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            buffer.write(current);
            if (isCrLf(previous, current) && buffer.size() >= 8) {
                break;
            }
            previous = current;
        }
        return buffer.toString(Charset.defaultCharset()).trim();
    }

    public static Map<String, String> readHeaders(InputStream inputStream) throws IOException {
        var buffer = new ByteArrayOutputStream(8 * 1024);
        var headers = new HashMap<String, String>();
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            buffer.write(current);
            if (isCrLf(previous, current)) {
                var header = buffer.toString(Charset.defaultCharset()).trim();
                if (header.isBlank()) {
                    break;
                }
                log.debug("Header: {}", header);
                var parts = header.split(": ");
                if (parts.length == 2) {
                    headers.put(parts[0], parts[1]);
                }
                buffer.reset();
            }
            previous = current;
        }
        return headers;
    }

    public static byte[] readGzipBody(InputStream inputStream) throws IOException {
        var gzip = new GZIPInputStream(inputStream);
        var writeBuffer = new ByteArrayOutputStream(8 * 1024);
        var readBuffer = ByteBuffer.allocate(8 * 1024);
        int byteCount;
        while ((byteCount = gzip.read(readBuffer.array())) != END_OF_STREAM) {
            readBuffer.position(0);
            writeBuffer.write(readBuffer.array(), 0, byteCount);
            readBuffer.clear();
        }
        return writeBuffer.toByteArray();
    }

    /**
     * CR (Carriage Return): ASCII value 13 (\r)
     * LF (Line Feed): ASCII value 10 (\n)
     */
    private static boolean isCrLf(int carriageReturn, int lineFeed) {
        return carriageReturn == '\r' && lineFeed == '\n';
    }
}
