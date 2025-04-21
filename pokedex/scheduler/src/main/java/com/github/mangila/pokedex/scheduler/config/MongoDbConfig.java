package com.github.mangila.pokedex.scheduler.config;

import com.github.mangila.pokedex.shared.repository.SchedulerAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoDbConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SchedulerAuditorAware();
    }
}
