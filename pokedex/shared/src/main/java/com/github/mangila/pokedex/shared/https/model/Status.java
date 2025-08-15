package com.github.mangila.pokedex.shared.https.model;

public record Status(String version, String code, String message) {

    public static Status from(String statusLine) {
        var split = statusLine.split(" ",3);
        return new Status(split[0], split[1], split[2]);
    }

}
