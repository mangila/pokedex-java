package com.github.mangila.pokedex.shared.database.internal.file;

public record OffsetBoundary(long start, long end) {

    public static OffsetBoundary from(long start, long end) {
        return new OffsetBoundary(start, end);
    }

}
