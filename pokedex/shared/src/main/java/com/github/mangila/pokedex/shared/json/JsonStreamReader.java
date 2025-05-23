package com.github.mangila.pokedex.shared.json;

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

    public CharBuffer read(int length) throws IOException {
        var buffer = CharBuffer.allocate(length);
        //noinspection ResultOfMethodCallIgnored
        stream.read(buffer.array(), 0, length);
        return buffer;
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
