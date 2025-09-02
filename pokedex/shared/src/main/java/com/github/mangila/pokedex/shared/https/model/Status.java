package com.github.mangila.pokedex.shared.https.model;

public record Status(String version, String code, String message) {

    public static Status from(String statusLine) {
        String[] split = statusLine.split(" ", 3);
        if (split.length < 3) {
            throw new IllegalArgumentException("Invalid status line: " + statusLine);
        }
        return new Status(split[0], split[1], split[2]);
    }

}
