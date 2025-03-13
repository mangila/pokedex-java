package com.github.mangila.pokedex.backstage.media.props;

import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.task")
public class MediaTaskProperties {

    private RedisStreamKey mediaStreamKey;

    public RedisStreamKey getMediaStreamKey() {
        return mediaStreamKey;
    }

    public void setMediaStreamKey(RedisStreamKey mediaStreamKey) {
        this.mediaStreamKey = mediaStreamKey;
    }
}
