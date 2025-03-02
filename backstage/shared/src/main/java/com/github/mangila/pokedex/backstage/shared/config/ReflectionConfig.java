package com.github.mangila.pokedex.backstage.shared.config;

import com.github.mangila.pokedex.backstage.shared.model.domain.*;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
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
