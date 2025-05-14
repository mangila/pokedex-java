package com.github.mangila.pokedex.shared.https.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.Map;

public class MagicNumberReader {

    public static final String JPEG = "jpeg";
    public static final String PNG = "png";
    public static final String GIF87a = "gif87a";
    public static final String GIF89a = "gif89a";
    public static final String GZIP = "gzip";
    public static final String OGG = "ogg";

    private static final int MAX_MAGIC_LENGTH = 16;
    private static final Map<String, byte[]> MAGIC_NUMBERS = Map.of(
            JPEG, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            PNG, new byte[]{(byte) 0x89, 'P', 'N', 'G'},
            GIF87a, "GIF87a".getBytes(),
            GIF89a, "GIF89a".getBytes(),
            GZIP, new byte[]{(byte) 0x1F, (byte) 0x8B},
            OGG, "OggS".getBytes()
    );

    private final PushbackInputStream input;

    public MagicNumberReader(InputStream input) {
        this.input = new PushbackInputStream(input, MAX_MAGIC_LENGTH);
    }

    public String getFormat() throws IOException {
        byte[] header = new byte[MAX_MAGIC_LENGTH];
        int bytesRead = input.read(header);
        if (bytesRead == -1) {
            throw new IOException("Stream ended unexpectedly");
        }
        input.unread(header, 0, bytesRead);

        for (var entry : MAGIC_NUMBERS.entrySet()) {
            var magicNumber = entry.getValue();
            if (Arrays.equals(magicNumber,
                    0, magicNumber.length,
                    header,
                    0, magicNumber.length)) {
                return entry.getKey();
            }
        }

        return "Unknown";
    }

    public PushbackInputStream getInputStream() {
        return input;
    }
}
