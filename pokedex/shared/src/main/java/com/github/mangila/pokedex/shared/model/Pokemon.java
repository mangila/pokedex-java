package com.github.mangila.pokedex.shared.model;

import com.github.mangila.pokedex.shared.database.DatabaseObject;

import java.io.*;

public record Pokemon(int id, String name) implements DatabaseObject<Pokemon> {

    public static final Pokemon DEFAULT_INSTANCE = new Pokemon(0, "MissingNo");

    @Override
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeInt(id);
            out.writeUTF(name);
            return buffer.toByteArray();
        }
    }

    @Override
    public Pokemon deserialize(byte[] data) throws IOException {
        try (ByteArrayInputStream buffer = new ByteArrayInputStream(data);
             DataInputStream in = new DataInputStream(buffer)) {
            int id = in.readInt();
            String name = in.readUTF();
            return new Pokemon(id, name);
        }
    }
}