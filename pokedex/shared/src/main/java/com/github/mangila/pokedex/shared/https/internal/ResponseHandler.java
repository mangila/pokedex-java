package com.github.mangila.pokedex.shared.https.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class ResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);
    private static final int END_OF_STREAM = -1;

    public static byte[] readStatusLine(InputStream inputStream) throws IOException {
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
        return buffer.toByteArray();
    }

    public static byte[] readHeaders(InputStream inputStream) throws IOException {
        var buffer = new ByteArrayOutputStream(8 * 1024);
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            buffer.write(current);
            if (isCrLf(previous, current) && hasDoubleCrlf(buffer.toByteArray())) {
                break;
            }
            previous = current;
        }
        return buffer.toByteArray();
    }

    public static byte[] readGzipBody(InputStream inputStream, int contentLength) throws IOException {
        var gzip = new GZIPInputStream(inputStream);
        var writeBuffer = new ByteArrayOutputStream(contentLength);
        var readBuffer = new byte[8 * 1024];
        int len;
        while ((len = gzip.read(readBuffer)) != END_OF_STREAM) {
            writeBuffer.write(readBuffer, 0, len);
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

    private static boolean hasDoubleCrlf(byte[] buffer) {
        var length = buffer.length;
        if (length < 4) {
            return false;
        }
        return isCrLf(buffer[length - 4], buffer[length - 3]) &&
                isCrLf(buffer[length - 2], buffer[length - 1]);
    }

}
