package com.github.mangila.pokedex.database.internal.io.data;

import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.internal.DatabaseFileHandler;
import com.github.mangila.pokedex.database.internal.io.internal.model.*;
import com.github.mangila.pokedex.database.internal.model.Value;
import com.github.mangila.pokedex.shared.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void init() throws IOException {
        databaseFileHandler.init();
    }

    public void truncate() throws IOException {
        LOGGER.info("Truncating data file");
        databaseFileHandler.fileModification().truncate();
    }

    public void delete() throws IOException {
        LOGGER.info("Deleting data file");
        databaseFileHandler.fileAccess().channelHandler().close();
        databaseFileHandler.fileModification().delete();
    }

    public Value read(Offset offset) throws IOException {
        Entry entry = databaseFileHandler.fileAccess().read(
                BufferUtils.newByteBuffer(Integer.BYTES),
                offset.value(),
                true);
        int recordLength = entry.getInt();
        entry = databaseFileHandler.fileAccess().read(
                BufferUtils.newByteBuffer(recordLength),
                offset.value() + Integer.BYTES,
                true);
        return new Value(entry.getArray());
    }

    public OffsetBoundary append(Value value) throws IOException {
        DataEntry dataEntry = new DataEntry(value.value());
        Entry entry = new Entry(dataEntry.toByteBuffer(true));
        return databaseFileHandler.fileAccess()
                .append(entry);
    }
}
