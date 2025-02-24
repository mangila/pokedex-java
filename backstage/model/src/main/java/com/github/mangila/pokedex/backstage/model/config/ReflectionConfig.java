package com.github.mangila.pokedex.backstage.model.config;

import com.github.mangila.pokedex.backstage.model.*;
import com.github.mangila.pokedex.backstage.model.grpc.redis.*;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding(classes = {
        // domain
        Generation.class,
        PokemonName.class,
        RedisStreamKey.class,
        RedisConsumerGroup.class,
        Task.class,
        // protos
        Redis.class,
        EntryRequest.class,
        EntryRequestOrBuilder.class,
        StreamRecord.class,
        StreamRecordOrBuilder.class
})
public class ReflectionConfig {
}
