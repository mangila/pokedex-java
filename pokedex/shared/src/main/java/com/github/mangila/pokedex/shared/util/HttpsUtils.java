package com.github.mangila.pokedex.shared.util;

import java.util.regex.Pattern;

public final class HttpsUtils {

    public static final int END_OF_STREAM = -1;
    public static final Pattern HEX_DECIMAL = Pattern.compile("^[0-9a-fA-F]+$");

    private HttpsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * CR (Carriage Return): ASCII value 13 (\r)
     * LF (Line Feed): ASCII value 10 (\n)
     */
    public static boolean isCrLf(int carriageReturn, int lineFeed) {
        return carriageReturn == '\r' && lineFeed == '\n';
    }

}
