package com.github.mangila.pokedex.shared.repository;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class SchedulerAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("scheduler");
    }
}
