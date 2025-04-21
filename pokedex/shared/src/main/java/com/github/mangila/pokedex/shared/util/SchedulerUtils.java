package com.github.mangila.pokedex.shared.util;

import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.util.Objects;

@lombok.experimental.UtilityClass
public class SchedulerUtils {

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
        var host = uri.getHost();
        if (Objects.equals(host, "pokeapi.co") || Objects.equals(host, "raw.githubusercontent.com")) {
            if (!Objects.equals(uri.getScheme(), "https")) {
                throw new IllegalArgumentException("should be 'https' - " + uri);
            }
        } else {
            throw new IllegalArgumentException("should start with 'raw.githubusercontent.com' or 'pokeapi.co' - " + uri);
        }
    }
}
