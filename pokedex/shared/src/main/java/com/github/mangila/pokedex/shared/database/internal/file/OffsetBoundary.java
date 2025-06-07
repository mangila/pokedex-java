package com.github.mangila.pokedex.shared.database.internal.file;

public record OffsetBoundary(long startOffset, long endOffset) {

    public static OffsetBoundary from(long startOffset, long endOffset) {
        return new OffsetBoundary(startOffset, endOffset);
    }
}
