package com.github.mangila.pokedex.backstage.pokemon.props;

import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.task")
public class PokemonTaskProperties {

    private RedisStreamKey nameStreamKey;
    private RedisStreamKey mediaStreamKey;

    public RedisStreamKey getNameStreamKey() {
        return nameStreamKey;
    }

    public void setNameStreamKey(RedisStreamKey nameStreamKey) {
        this.nameStreamKey = nameStreamKey;
    }

    public RedisStreamKey getMediaStreamKey() {
        return mediaStreamKey;
    }

    public void setMediaStreamKey(RedisStreamKey mediaStreamKey) {
        this.mediaStreamKey = mediaStreamKey;
    }
}
