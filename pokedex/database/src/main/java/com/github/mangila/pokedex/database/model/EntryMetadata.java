package com.github.mangila.pokedex.database.model;

import java.util.Map;

public class EntryMetadata {
    private final Map<BoundaryType, OffsetBoundary> boundaries;
    private boolean tombstone;

    public EntryMetadata(Map<BoundaryType, OffsetBoundary> boundaries, boolean tombstone) {
        this.boundaries = boundaries;
        this.tombstone = tombstone;
    }

    public enum BoundaryType {
        KEY, FIELD, VALUE, TOMBSTONE;
    }

    public OffsetBoundary getBoundary(BoundaryType boundaryType) {
        return boundaries.get(boundaryType);
    }

    public boolean isTombstone() {
        return tombstone;
    }

    public void setTombstone(boolean tombstone) {
        this.tombstone = tombstone;
    }
}
