package com.github.mangila.pokedex.shared.database.internal.file.data;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.database.DatabaseObject;
import com.github.mangila.pokedex.shared.database.internal.file.DatabaseFileName;
import com.github.mangila.pokedex.shared.database.internal.file.File;
import com.github.mangila.pokedex.shared.database.internal.file.data.header.DataFileHeaderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.zip.CRC32C;

/**
 * File Structure Layout for Data
 * <p>
 * File Header Section:
 * <pre>
 * +----------------+--------------------+----------------+
 * | Magic Number   | "yakvsdta" bytes  | File identifier|
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
 * | CRC32C Checksum | 8 bytes            |
 * +-----------------+----------------------+
 * </pre>
 * <p>
 * Records are stored sequentially with CRC32C checksums for data integrity.
 */
public class DataFileHandler<V extends DatabaseObject<V>> {

    private static final Logger log = LoggerFactory.getLogger(DataFileHandler.class);

    private final ThreadLocal<CRC32C> crc32CThreadLocal = ThreadLocal.withInitial(CRC32C::new);
    private final File file;
    private final DataFileHeaderHandler headerHandler;
    private final DataReader dataReader;
    private final DataWriter dataWriter;

    public DataFileHandler(DatabaseName databaseName) {
        var fileName = databaseName.value()
                .concat(".data")
                .concat(".yakvs");
        this.file = new File(new DatabaseFileName(fileName));
        this.headerHandler = new DataFileHeaderHandler(file);
        this.dataReader = new DataReader(file);
        this.dataWriter = new DataWriter(file);
    }

    public void init() throws IOException {
        file.tryCreateFileIfNotExists();
        if (file.isEmpty()) {
            headerHandler.write();
        } else {
            headerHandler.load();
        }
    }

    public void writeNewRecord(String key, V value) throws IOException {
        long offset = headerHandler.getOffset();
        long newOffset = dataWriter.write(key,
                value.serialize(),
                offset,
                crc32CThreadLocal.get());
        headerHandler.update(newOffset);
        log.debug("Inserted record {} at {} -- new offset {}", key, offset, newOffset);
    }

    public void deleteFile() throws IOException {
        log.info("Deleting file {}", file.getPath().getFileName());
        file.deleteFile();
    }

    public byte[] read(long offset) throws IOException {
        var record = dataReader.read(offset);
        return record.data();
    }

    public long getOffset() {
        return headerHandler.getOffset();
    }
}
