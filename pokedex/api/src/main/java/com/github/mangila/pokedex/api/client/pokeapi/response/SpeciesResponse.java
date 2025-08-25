package com.github.mangila.pokedex.api.client.pokeapi.response;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.shared.json.model.JsonArray;
import com.github.mangila.pokedex.shared.json.model.JsonObject;
import com.github.mangila.pokedex.shared.json.model.JsonRoot;

import java.math.BigInteger;
import java.util.List;

public record SpeciesResponse(BigInteger id,
                              String name,
                              String description,
                              String genus,
                              String color,
                              PokeApiUri evolutionChainUrl,
                              Pedigree pedigree,
                              List<PokeApiUri> varietiesUrls) {
    public static SpeciesResponse from(JsonRoot jsonRoot) {
        return new SpeciesResponseMapper()
                .map(jsonRoot);
    }

    public record Pedigree(boolean baby, boolean legendary, boolean mythical) {
        public byte[] getBabyAsBytes() {
            return new byte[]{(byte) (baby ? 1 : 0)};
        }

        public byte[] getLegendaryAsBytes() {
            return new byte[]{(byte) (legendary ? 1 : 0)};
        }

        public byte[] getMythicalAsBytes() {
            return new byte[]{(byte) (mythical ? 1 : 0)};
        }
    }

    private static final class SpeciesResponseMapper implements JsonMapper<SpeciesResponse> {
        @Override
        public SpeciesResponse map(JsonRoot jsonRoot) {
            BigInteger id = getId(jsonRoot);
            String name = getName(jsonRoot);
            String description = getDescription(jsonRoot.getArray("flavor_text_entries"));
            String genus = getGenus(jsonRoot.getArray("genera"));
            String color = getColor(jsonRoot.getObject("color"));
            PokeApiUri evolutionChainUrl = getEvolutionChainUrl(jsonRoot.getObject("evolution_chain"));
            Pedigree pedigree = getPedigree(jsonRoot);
            List<PokeApiUri> varietiesUrls = getVarietiesUrls(jsonRoot.getArray("varieties"));
            return new SpeciesResponse(id, name, description, genus, color, evolutionChainUrl, pedigree, varietiesUrls);
        }

        private static BigInteger getId(JsonRoot jsonRoot) {
            return (BigInteger) jsonRoot.getNumber("id");
        }

        private static String getName(JsonRoot jsonRoot) {
            return jsonRoot.getString("name");
        }

        private static String getDescription(JsonArray flavor_text_entries) {
            return flavor_text_entries.values()
                    .stream()
                    .filter(jsonValue -> jsonValue.unwrapObject().getObject("language").getString("name").equals("en"))
                    .findFirst()
                    .orElseThrow()
                    .unwrapObject()
                    .getString("flavor_text");
        }

        private static String getGenus(JsonArray genera) {
            return genera.values()
                    .stream()
                    .filter(entry -> entry.unwrapObject().getObject("language").getString("name").equals("en"))
                    .findFirst()
                    .orElseThrow()
                    .unwrapObject()
                    .getString("genus");
        }

        private static String getColor(JsonObject color) {
            return color.getString("name");
        }

        private static PokeApiUri getEvolutionChainUrl(JsonObject evolution_chain) {
            String url = evolution_chain.getString("url");
            return PokeApiUri.from(url);
        }

        private static Pedigree getPedigree(JsonRoot jsonRoot) {
            boolean baby = jsonRoot.getBoolean("is_baby");
            boolean legendary = jsonRoot.getBoolean("is_legendary");
            boolean mythical = jsonRoot.getBoolean("is_mythical");
            return new Pedigree(baby, legendary, mythical);
        }

        private static List<PokeApiUri> getVarietiesUrls(JsonArray varieties) {
            return varieties.values()
                    .stream()
                    .map(variety -> PokeApiUri.from(variety.unwrapObject()
                            .getObject("pokemon")
                            .getString("url")))
                    .toList();
        }
    }
}
