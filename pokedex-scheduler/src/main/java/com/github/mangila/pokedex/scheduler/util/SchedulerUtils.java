package com.github.mangila.pokedex.scheduler.util;

import org.springframework.http.MediaType;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.util.Objects;

@lombok.experimental.UtilityClass
public class SchedulerUtils {

    public String getContentType(String fileName) {
        return switch (UriUtils.extractFileExtension(fileName)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "svg" -> "image/svg+xml";
            case "ogg" -> "audio/ogg";
            case null -> throw new NullPointerException();
            default -> throw new IllegalArgumentException("Unsupported file extension: " + fileName);
        };
    }

    public String createFileName(String name, String suffix, URI uri) {
        return new StringBuilder()
                .append(name)
                .append("-")
                .append(suffix)
                .append(".")
                .append(UriUtils.extractFileExtension(uri.toString()))
                .toString();
    }

    public void ensureUriFromPokeApi(URI uri) {
        if (Objects.isNull(uri)) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        switch (uri.getHost()) {
            case "raw.githubusercontent.com", "pokeapi.co":
                return;
            default:
                throw new IllegalArgumentException("should start with 'raw.githubusercontent.com' or 'pokeapi.co' - " + uri);
        }
    }
}
