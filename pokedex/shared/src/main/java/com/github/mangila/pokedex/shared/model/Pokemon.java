package com.github.mangila.pokedex.shared.model;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public record Pokemon(int id, String name) {

    public byte[] toBytes() {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeInt(id);
            out.writeUTF(name);
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Pokemon to bytes", e);
        }
    }

    public static Pokemon fromBytes(byte[] bytes) {
        try (ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
             DataInputStream in = new DataInputStream(buffer)) {
            int id = in.readInt();
            String name = in.readUTF();
            return new Pokemon(id, name);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Pokemon from bytes", e);
        }
    }
}