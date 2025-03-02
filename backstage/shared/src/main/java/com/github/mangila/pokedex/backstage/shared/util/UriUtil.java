package com.github.mangila.pokedex.backstage.shared.util;

import java.net.URI;

public final class UriUtil {

    private UriUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getLastPathSegment(URI uri) {
        String path = uri.getPath();
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);
        if (lastSegment.isBlank()) {
            lastSegment = path.substring(path.lastIndexOf("/", path.length() - 2));
            lastSegment = lastSegment.replaceAll("/", "");
        }
        return lastSegment;
    }

}
