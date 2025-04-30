package com.github.mangila.pokedex.shared.pokeapi;

class Utils {

    private Utils() {
        throw new IllegalStateException("I'm a utility class");
    }

    /**
     * CR (Carriage Return): ASCII value 13 (\r)
     * LF (Line Feed): ASCII value 10 (\n)
     */
    static boolean IsCrLf(int carriageReturn, int lineFeed) {
        return carriageReturn == '\r' && lineFeed == '\n';
    }
}
