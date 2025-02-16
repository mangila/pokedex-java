package com.github.mangila.model.domain;

import org.springframework.http.MediaType;

import java.io.Serializable;
import java.net.URL;

public record PokemonMedia(
        PokemonId id,
        PokemonName name,
        String description,
        URL url
) implements Serializable {

    /**
     * @return {name}-{description}.{fileExtension}
     */
    public String buildFileName() {
        return new StringBuilder()
                .append(name.getName())
                .append("-")
                .append(description)
                .append(".")
                .append(buildFileExtension())
                .toString();
    }

    public String buildFileExtension() {
        var url = this.url();
        String path = url.getPath();
        String[] pathSegments = path.split("/");
        String lastSegment = pathSegments[pathSegments.length - 1];
        return lastSegment.substring(lastSegment.lastIndexOf('.') + 1);
    }

    public String createContentType() {
        var extension = buildFileExtension();
        return switch (extension) {
            case "jpg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "ogg" -> "audio/ogg";
            default -> throw new IllegalArgumentException("Unknown extension: " + extension);
        };
    }

}
