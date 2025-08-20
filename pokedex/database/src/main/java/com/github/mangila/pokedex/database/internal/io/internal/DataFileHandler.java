package com.github.mangila.pokedex.database.internal.io.internal;

import com.github.mangila.pokedex.database.DatabaseName;
import com.github.mangila.pokedex.database.internal.io.internal.model.*;
import com.github.mangila.pokedex.database.internal.io.internal.util.FileChannelUtil;
import com.github.mangila.pokedex.database.internal.io.model.DatabaseFileName;
import com.github.mangila.pokedex.database.internal.io.model.Value;
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
        if (databaseFileHandler.databaseFile().isEmpty()) {
            LOGGER.info("Creating new data file");
            FileChannelUtil.writeHeader(
                    databaseFileHandler.channel(),
                    DatabaseFileHeader.EMPTY
            );
        } else {
            LOGGER.info("Loading existing data file");
        }
    }

    public void truncate() throws IOException {
        databaseFileHandler.truncate();
    }

    public void deleteFile() throws IOException {
        databaseFileHandler.deleteFile();
    }

    public Value read(Offset offset) throws IOException {
        Entry entry = FileChannelUtil.read(
                BufferUtils.newByteBuffer(Integer.BYTES),
                offset.value(),
                true,
                databaseFileHandler.channel());
        int recordLength = entry.getInt();
        entry = FileChannelUtil.read(
                BufferUtils.newByteBuffer(recordLength),
                offset.value() + Integer.BYTES,
                true,
                databaseFileHandler.channel());
        return new Value(entry.getArray());
    }

    public OffsetBoundary append(Value value) throws IOException {
        DataEntry dataEntry = new DataEntry(value.value());
        Entry entry = new Entry(dataEntry.toByteBuffer(true));
        return FileChannelUtil.append(entry, databaseFileHandler.channel());
    }
}
