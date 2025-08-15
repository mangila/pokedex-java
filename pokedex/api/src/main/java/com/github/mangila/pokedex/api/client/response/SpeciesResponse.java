package com.github.mangila.pokedex.api.client.response;

import com.github.mangila.pokedex.api.client.PokeApiUri;
import com.github.mangila.pokedex.shared.json.model.JsonTree;

import java.util.List;

// FIXME - mapping stuffs
public record SpeciesResponse(int id,
                              String name,
                              String description,
                              String genus,
                              String color,
                              PokeApiUri evolutionChainUrl,
                              Pedigree pedigree,
                              List<PokeApiUri> varietiesUrls) {
    public static SpeciesResponse from(JsonTree jsonTree) {
        int id = jsonTree.getValue("id").getNumber().intValue();
        String name = jsonTree.getValue("name").getString();
        String description = jsonTree.getValue("flavour_text_entries")
                .getArray()
                .values()
                .stream()
                .filter(entry -> entry.getObject().getString("language").equals("en"))
                .findFirst()
                .orElseThrow()
                .getObject()
                .getString("flavor_text");
        String genus = jsonTree.getValue("genera")
                .getArray()
                .values()
                .stream()
                .filter(entry -> entry.getObject().getString("language").equals("en"))
                .findFirst()
                .orElseThrow()
                .getObject()
                .getString("genus");
        String color = jsonTree.getObject("color")
                .getString("name");
        PokeApiUri evolutionChain = PokeApiUri.fromString(jsonTree.getObject("evolution_chain")
                .getString("url"));
        Pedigree pedigree = new Pedigree(
                jsonTree.getBoolean("is_baby"),
                jsonTree.getBoolean("is_legendary"),
                jsonTree.getBoolean("is_mythical")
        );
        List<PokeApiUri> varietiesUrls = jsonTree.getValue("varieties")
                .getArray()
                .values()
                .stream()
                .map(variety -> PokeApiUri.fromString(variety.getObject()
                        .getObject("pokemon")
                        .getString("url")))
                .toList();
        return new SpeciesResponse(id, name, description, genus, color, evolutionChain, pedigree, varietiesUrls);
    }

    public record Pedigree(boolean baby, boolean legendary, boolean mythical) {
    }
}
