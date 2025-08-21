package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.internal.io.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.internal.model.Buffer;
import com.github.mangila.pokedex.database.internal.io.internal.model.DataEntry;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;

import java.io.IOException;

/**
 * File Structure Layout for Data
 * <p>
 * File Header Section:
 * <pre>
 * +----------------+--------------------+----------------+
 * | Magic Number   | "yakvs" bytes     | File identifier|
 * | Version        | 4 bytes           | Format version |
 * | Record Count   | 4 bytes           | Num records    |
 * | Next Offset    | 8 bytes           | Write position |
 * +----------------+--------------------+----------------+
 * </pre>
 * <p>
 * Data Records Section:
 * <pre>
 * +-----------------+----------------------+
 * | Record Length   | 4 bytes             |
 * | Data            | Variable length     |
 * +-----------------+----------------------+
 * </pre>
 * <p>
 * Records are stored sequentially
 */
public final class DataFileHandler extends AbstractFileHandler {

    public DataFileHandler(DatabaseFileName databaseFileName) {
        super(new DatabaseFile(databaseFileName));
    }
    public DataEntry read(Offset offset) throws IOException {
        Buffer buffer = fileAccess().read(
                Buffer.from(Integer.BYTES),
                offset,
                true);
        buffer = fileAccess().read(
                Buffer.from(buffer.getInt()),
                new Offset(Integer.BYTES + offset.value()),
                true);
        return new DataEntry(buffer.getArray());
    }
}
