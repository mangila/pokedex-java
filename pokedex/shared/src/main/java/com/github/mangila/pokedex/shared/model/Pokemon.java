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
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Pokemon to bytes", e);
        }
    }

    @Override
    public Pokemon deserialize(byte[] data) throws IOException {
        try (ByteArrayInputStream buffer = new ByteArrayInputStream(data);
             DataInputStream in = new DataInputStream(buffer)) {
            int id = in.readInt();
            String name = in.readUTF();
            return new Pokemon(id, name);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Pokemon from bytes", e);
        }
    }
}