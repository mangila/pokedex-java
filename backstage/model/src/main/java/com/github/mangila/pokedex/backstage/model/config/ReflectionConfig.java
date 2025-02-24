package com.github.mangila.pokedex.backstage.model.config;

import com.github.mangila.pokedex.backstage.model.*;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@RegisterReflectionForBinding(classes = {
        Generation.class,
        PokemonName.class,
        RedisStreamKey.class,
        RedisConsumerGroup.class,
        Task.class,
})
@ImportRuntimeHints(ProtobufRuntimeHints.class)
public class ReflectionConfig {

}
