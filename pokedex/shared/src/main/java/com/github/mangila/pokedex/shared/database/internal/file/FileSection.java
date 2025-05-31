package com.github.mangila.pokedex.shared.database.internal.file;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public final class FileSection {

    public static Map<String, Long> loadIndexes(File file,
                                                long indexOffset,
                                                long dataOffset,
                                                int recordCount) throws IOException {
        long indexSize = dataOffset - indexOffset;
        var buffer = file.getReadChannel().map(
                FileChannel.MapMode.READ_ONLY,
                indexOffset,
                indexSize);
        var indexMap = new HashMap<String, Long>();
        for (int i = 0; i < recordCount; i++) {
            int keyLength = buffer.getInt();
            byte[] keyBytes = new byte[keyLength];
            buffer.get(keyBytes);
            long dataPos = buffer.getLong();
            String key = new String(keyBytes);
            indexMap.put(key, dataPos);
        }
        return indexMap;
    }

    public static void writeRecord() {
    }

    public static void writeIndex() {
    }
}
