package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.database.DatabaseName;
import com.github.mangila.pokedex.shared.model.Pair;

import java.io.IOException;
import java.util.zip.CRC32C;

/**
 * File Structure Layout for Data
 * <p>
 * File Header Section:
 * <pre>
 * +----------------+--------------------+----------------+
 * | Magic Number   | "yakvs" bytes  | File identifier|
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
public class DataFileHandler {

    private final File file;
    private final CRC32C crc32c;
    private FileHeader header;

    public DataFileHandler(DatabaseName databaseName) {
        String fileName = databaseName.value()
                .concat(".data")
                .concat(".yakvs");
        this.file = new File(new FileName(fileName));
        this.header = FileHeader.defaultValue();
        this.crc32c = new CRC32C();
    }

    /**
     * Initializes the data file. If the file does not exist, it will be created. If the file is empty,
     * a default header will be written to it. Otherwise, the method will read and parse the existing header from the file.
     *
     * @throws IOException if there is an issue, creating the file, reading from it, or writing to it.
     */
    public void init() throws IOException {
        file.tryCreateFileIfNotExists();
        if (file.isEmpty()) {
            file.write(header.toByteBuffer(), 0);
        } else {
            var buffer = file.readAndFlip(0, FileHeader.HEADER_SIZE);
            this.header = FileHeader.from(buffer);
        }
    }

    public Pair<Long, Long> write(byte[] data) throws IOException {
        long offset = header.offset();
        var record = DataRecord.from(data, crc32c);
        int size = record.getSize();
        file.write(record.toByteBuffer(), offset);
        long newOffset = offset + size;
        return new Pair<>(offset, newOffset);
    }

    public void write(long dataOffset, DataRecord record) throws IOException {
        file.write(record.toByteBuffer(), dataOffset);
    }

    public Pair<Boolean, Integer> updateIfSameSize(long dataOffset, byte[] data) throws IOException {
        var record = DataRecord.from(data, crc32c);
        int size = record.getSize();
        var existingRecord = read(dataOffset);
        if (existingRecord.getSize() == size) {
            write(dataOffset, record);
            return new Pair<>(true, size);
        }
        return new Pair<>(false, size);
    }

    public DataRecord read(long dataOffset) throws IOException {
        var buffer = file.readAndFlip(dataOffset, Integer.BYTES);
        var length = buffer.getInt();
        buffer = file.readAndFlip(
                dataOffset + Integer.BYTES,
                length);
        var data = buffer.array();
        buffer = file.readAndFlip(
                dataOffset + Integer.BYTES + length,
                Long.BYTES);
        var checksum = buffer.getLong();
        return new DataRecord(data.length, data, checksum);
    }

    public void updateHeader(long newOffset) throws IOException {
        header = header.updateOffset(newOffset);
        file.write(header.toByteBuffer(), 0);
    }

    public void deleteFile() throws IOException {
        file.tryDeleteFile();
    }

    public void truncate() throws IOException {
        file.truncate();
        header = FileHeader.defaultValue();
        file.write(header.toByteBuffer(), 0);
    }
}
