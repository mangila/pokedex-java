package com.github.mangila.pokedex.api.graphql.resolver;

import com.github.mangila.pokedex.api.graphql.model.PokemonSpecies;
import com.github.mangila.pokedex.api.graphql.service.PokemonService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@lombok.AllArgsConstructor
public class PokemonResolver {

    private final PokemonService pokemonService;

    @QueryMapping
    public PokemonSpecies findById(@Argument Integer id) {
        return pokemonService.findById(id);
    }

    @QueryMapping
    public PokemonSpecies findByName(@Argument String name) {
        return pokemonService.findByName(name);
    }

}
