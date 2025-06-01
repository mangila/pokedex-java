package com.github.mangila.pokedex.shared.https.model;

public record HttpStatus(String version, String code, String message) {

    public static HttpStatus fromString(String statusLine) {
        var split = statusLine.split(" ",3);
        return new HttpStatus(split[0], split[1], split[2]);
    }

}
