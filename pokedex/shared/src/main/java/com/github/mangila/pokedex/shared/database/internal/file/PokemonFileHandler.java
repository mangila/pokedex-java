package com.github.mangila.pokedex.shared.database.internal.file;

import com.github.mangila.pokedex.shared.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32C;

import static com.github.mangila.pokedex.shared.database.internal.file.FormatOptions.*;

public class PokemonFileHandler {

    private static final Logger log = LoggerFactory.getLogger(PokemonFileHandler.class);

    private final Map<String, Long> keyOffset = new ConcurrentHashMap<>();
    private final AtomicInteger pokemonCount = new AtomicInteger(0);
    private final ThreadLocal<CRC32C> crc32c = ThreadLocal.withInitial(CRC32C::new);
    private final PokemonFile pokemonFile;

    public PokemonFileHandler(PokemonFile pokemonFile) {
        this.pokemonFile = pokemonFile;
        try {
            if (pokemonFile.isEmpty()) {
                init();
            } else {
                load();
            }
        } catch (IOException e) {
            log.error("ERR", e);
            throw new RuntimeException(e);
        }
    }

    public long write(String key, Pokemon pokemon) throws IOException {
        return -1L;
    }

    public Pokemon read(String key) throws IOException {
        return null;
    }

    public void deleteFile() {
        try {
            log.info("Deleting file {}", pokemonFile.getPath().getFileName());
            pokemonFile.getReadChannel().close();
            pokemonFile.getWriteChannel().close();
            Files.deleteIfExists(pokemonFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the file header with initial placeholder values.
     * <p>
     * The header layout is as follows (byte offsets and sizes):
     * <ul>
     *   <li><b>Magic Number</b>: 7 bytes — ASCII bytes for "Pok3mon"</li>
     *   <li><b>Version</b>: 1 byte — file format version</li>
     *   <li><b>Record Count</b>: 4 bytes — number of records (initialized to 0)</li>
     *   <li><b>Index Offset</b>: 8 bytes — file offset where index section starts (initialized to 0)</li>
     *   <li><b>Data Offset</b>: 8 bytes — file offset where data section starts (HEADER_SIZE)</li>
     * </ul>
     * <p>
     * Total header size is {@code HEADER_SIZE} bytes.
     * </p>
     *
     * <p><b>Note:</b>
     * Initial values for Record Count and Index Offset are zero placeholders and
     * should be updated after writing records and index sections.
     * </p>
     */
    private void init() throws IOException {
        MappedByteBuffer buffer = pokemonFile.getWriteChannel().map(
                FileChannel.MapMode.READ_WRITE,
                0,
                HEADER_SIZE);
        buffer.put(POKEMON_MAGIC_NUMBER_BYTES);
        buffer.putInt(VERSION);
        buffer.putInt(pokemonCount.get());
        buffer.putLong(keyOffset.size());
        buffer.putLong(HEADER_SIZE);
        buffer.force();
    }

    private void load() throws IOException {
    }
}
