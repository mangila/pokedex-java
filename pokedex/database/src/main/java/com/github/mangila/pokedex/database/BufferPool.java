package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.shared.util.Ensure;

public record BufferPool() {

    public static int nextPowerOfTwo(int n) {
        Ensure.min(1, n);
        int highest = Integer.highestOneBit(n);
        return (n == highest) ? n : highest * 2;
    }
}
