package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.internal.model.DatabaseFile;
import com.github.mangila.pokedex.database.internal.io.internal.model.Offset;
import com.github.mangila.pokedex.database.internal.io.model.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.model.Key;
import com.github.mangila.pokedex.database.internal.io.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DataFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileHandler.class);

    private final DatabaseFileHandler databaseFileHandler;

    public DataFileHandler(DatabaseName databaseName) {
        DatabaseFileName databaseFileName = new DatabaseFileName(databaseName.value()
                .concat(".data")
                .concat(".yakvs"));
        DatabaseFile databaseFile = new DatabaseFile(databaseFileName);
        this.databaseFileHandler = new DatabaseFileHandler(databaseFile);
    }

    public void init() {
    }

    public Value read(Offset offset) {
        return null;
    }

    public Offset write(Key key, Value value) {
        LOGGER.debug("Writing key {} with value {}", key, value);
        return null;
    }
}
