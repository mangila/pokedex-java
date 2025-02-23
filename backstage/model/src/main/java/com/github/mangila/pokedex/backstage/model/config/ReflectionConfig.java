package com.github.mangila.pokedex.backstage.model.config;

import com.github.mangila.pokedex.backstage.model.*;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding(classes = {
        Generation.class,
        PokemonName.class,
        RedisCacheName.class,
        RedisQueueName.class,
        Task.class
})
public class ReflectionConfig {
}
