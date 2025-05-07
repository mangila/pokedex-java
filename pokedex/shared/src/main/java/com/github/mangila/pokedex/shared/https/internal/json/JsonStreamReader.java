package com.github.mangila.pokedex.shared.https.internal.json;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class JsonStreamReader implements AutoCloseable {

    private final BufferedReader stream;

    public JsonStreamReader(byte[] data) {
        this.stream = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), Charset.defaultCharset()));
    }

    public int read() throws IOException {
        return stream.read();
    }

    public CharBuffer peek(int offset) throws IOException {
        stream.mark(offset);
        var peek = CharBuffer.allocate(offset);
        //noinspection ResultOfMethodCallIgnored
        stream.read(peek.array(), 0, offset);
        stream.reset();
        return peek;
    }

    public void skip(int n) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        stream.skip(n);
    }

    @Override
    public void close() {
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
