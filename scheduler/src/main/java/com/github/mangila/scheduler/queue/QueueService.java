package com.github.mangila.scheduler.queue;

import com.github.mangila.model.domain.PokemonName;
import lombok.AllArgsConstructor;
import lombok.Locked;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;

@Service
@AllArgsConstructor
public class QueueService {

    private final ArrayDeque<PokemonName> queue = new ArrayDeque<>(1000);

    @Locked.Write
    public void add(PokemonName pokemonName) {
        queue.add(pokemonName);
    }

    @Locked.Read
    public PokemonName poll() {
        return queue.poll();
    }

}
