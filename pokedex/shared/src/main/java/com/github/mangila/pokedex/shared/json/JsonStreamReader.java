package com.github.mangila.pokedex.shared.json;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public record JsonStreamReader(BufferedReader stream) implements AutoCloseable {
    public JsonStreamReader(byte[] stream) {
        this(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stream), Charset.defaultCharset())));
    }

    public int read() throws IOException {
        return stream.read();
    }

    public CharBuffer read(int length) throws IOException {
        CharBuffer buffer = CharBuffer.allocate(length);
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
