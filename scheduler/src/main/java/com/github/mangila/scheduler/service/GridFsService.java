package com.github.mangila.scheduler.service;

import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.repository.GridFsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@AllArgsConstructor
public class GridFsService {

    private final GridFsRepository gridFsRepository;

    public String store(PokemonMedia pokemonMedia, byte[] bytes) {
        var content = new ByteArrayInputStream(bytes);
        var id = gridFsRepository.store(content,
                pokemonMedia.buildFileName(),
                pokemonMedia.createContentType());
        return id.toString();
    }

}
