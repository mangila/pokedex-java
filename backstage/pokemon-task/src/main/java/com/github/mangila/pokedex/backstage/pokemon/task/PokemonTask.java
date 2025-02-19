package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.shared.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PokemonTask implements Task {

    Logger log = LoggerFactory.getLogger(PokemonTask.class);

    @Override
    public void run() {
        log.info("Starting Pokemon Task");
    }
}
