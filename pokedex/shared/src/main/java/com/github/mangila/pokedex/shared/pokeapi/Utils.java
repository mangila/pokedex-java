package com.github.mangila.pokedex.shared.pokeapi;

final class Utils {

    private Utils() {
        throw new IllegalStateException("I'm a utility class!!! Leave this constructor alone!!!");
    }

    // carnage return, low fee
    static boolean IsCrLf(int cr, int lf) {
        return cr == '\r' && lf == '\n';
    }
}
