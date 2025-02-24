package com.github.mangila.pokedex.backstage.model.config;

import com.github.mangila.pokedex.backstage.model.Generation;
import com.github.mangila.pokedex.backstage.model.PokemonName;
import com.github.mangila.pokedex.backstage.model.Task;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding(classes = {
        Generation.class,
        PokemonName.class,
        Task.class
})
public class ReflectionConfig {
}
