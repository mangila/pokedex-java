package com.github.mangila.pokedex.shared;

import com.github.mangila.pokedex.shared.database.DatabaseConfig;
import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.model.Pokemon;
import org.junit.jupiter.api.Test;

class PokemonDatabaseTest {

    @Test
    void abc() {
        PokemonDatabase.configure(
                new DatabaseConfig(
                        new DatabaseName("pokedex"), 1));
        var db = PokemonDatabase.getInstance();
        db.get().init();
        db.get().put("ivysaur", new Pokemon(22, "ivysaur"));
        db.get().put("venosaur", new Pokemon(33, "venosaur"));
        db.get().put("chraizard", new Pokemon(77, "chraizard"));
        var b = db.get().get("venosaur");
        System.out.println(b.id());
        System.out.println(b.name());
        db.get().deleteFile();
    }

}